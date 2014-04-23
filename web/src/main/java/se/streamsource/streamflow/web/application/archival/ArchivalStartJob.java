/**
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.archival;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.*;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.ArchivalSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseDescriptor;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.casetype.ArchivalSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 *  This class performs archival tasks.
 */
@Mixins(ArchivalStartJob.Mixin.class)
public interface ArchivalStartJob extends InterruptableJob, TransientComposite {

    public String performArchivalCheck();
    public void performArchival() throws UnitOfWorkCompletionException;

    abstract class Mixin implements ArchivalStartJob {
        @Service
        FileConfiguration fileConfiguration;

        @Service
        PdfGeneratorService pfdGenerator;

        @Service
        AttachmentStoreService attachmentStore;

        File archiveDir;

        @Structure
        Module module;

        @Service
        ArchivalService archivalService;

        Usecase performArchival = UsecaseBuilder.newUsecase("Perform Archival");

        Logger logger = LoggerFactory.getLogger(ArchivalStartJob.class);

        boolean interruptRequest = false;
        int toArchive = 0;
        int forDelete = 0;

        public String performArchivalCheck()
        {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(performArchival);
            toArchive = 0;
            forDelete = 0;
            try
            {
                for (CaseEntity caseEntity : archivableCases(archivalSettings()))
                {
                    CaseType caseType = caseEntity.caseType().get();
                    if( ((Removable.Data)caseEntity).removed().get() )
                    {
                        logger.debug("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + (caseType == null ? "" : ", " + caseType.getDescription()) + "), created on " + caseEntity.createdOn().get() + ",is marked for remove and can be deleted");
                        forDelete++;
                    } else
                    {
                        logger.debug("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + (caseType == null ? "" : ", " + caseType.getDescription()) + "), created on " + caseEntity.createdOn().get() + ", can be archived");
                        toArchive++;
                    }
                }
            } finally
            {
                uow.discard();
            }

            return "" + toArchive + " cases can be archived.\r\n" + forDelete + " cases are marked for delete.";
        }

        public void performArchival() throws UnitOfWorkCompletionException
        {
            // check that archive directory exists or create otherwise
            archiveDir = new File(fileConfiguration.dataDirectory(), "archive");
            archiveDir.mkdir();

            // reset interruption
            interruptRequest = false;
            toArchive = 0;
            forDelete = 0;

            DateTime start = new DateTime( );
            logger.info( "Archival started: " + start.toString( "yyyy-MM-dd hh:mm:ss" ) );
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(performArchival);

            RoleMap.newCurrentRoleMap();
            RoleMap.current().set( uow.get( UserAuthentication.class, UserEntity.ADMINISTRATOR_USERNAME ) );
            RoleMap.current().set( new UserPrincipal( UserEntity.ADMINISTRATOR_USERNAME ) );

            try
            {
                int count = 1;
                final List<String> archivalSettingsIds = new ArrayList<String>();
                Inputs.iterable( archivalSettings()).transferTo( Outputs.withReceiver(new Receiver<ArchivalSettings.Data, Throwable>() {
                    @Override
                    public void receive(ArchivalSettings.Data item) throws Throwable {
                        archivalSettingsIds.add( item.toString() );
                    }
                }));

                for (String archivalSettingsId :archivalSettingsIds )
                {
                    ArchivalSettings.Data data = uow.get(ArchivalSettings.Data.class, archivalSettingsId );
                    ArchivalSettingsDTO settings = data.archivalSettings().get();

                    final List<String> archivableCaseIds = new ArrayList<String>();
                    Inputs.iterable(archivableCases(Iterables.iterable(data))).transferTo( Outputs.withReceiver( new Receiver<CaseEntity, Throwable>() {
                        @Override
                        public void receive(CaseEntity item) throws Throwable {
                            archivableCaseIds.add(item.toString());
                        }
                    }));

                    for (String caseId : archivableCaseIds )
                    {
                        CaseEntity caseEntity = uow.get(CaseEntity.class, caseId );

                        if( count++ % archivalService.configuration().modulo().get() == 0 )
                        {
                            uow.complete();
                            uow = module.unitOfWorkFactory().newUnitOfWork(performArchival);
                            data = uow.get( ArchivalSettings.Data.class, archivalSettingsId );
                            settings = data.archivalSettings().get();
                            caseEntity = uow.get(caseEntity);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e)
                            {
                            }
                        }
                        // if scheduler called for an interrupt - abort archival
                        // uow complete is called in the finally clause!!
                        if( interruptRequest )
                        {
                            break;
                        }

                        if (settings.archivalType().get().equals(ArchivalSettingsDTO.ArchivalType.delete))
                        {
                            try
                            {
                                logger.debug("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", was deleted");
                                caseEntity.deleteEntity();
                                forDelete++;
                            } catch (Exception e)
                            {
                                logger.warn("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", could not be archived", e);
                            }
                        } else if (settings.archivalType().get().equals(ArchivalSettingsDTO.ArchivalType.export))
                        {
                            try
                            {
                                if( !((Removable.Data)caseEntity).removed().get() )
                                {
                                    // if case is not marked as removed( soft delete ) -  create and export pdf
                                    File pdf = exportPdf(caseEntity);
                                    logger.debug("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", was archived");

                                    // archiving attachments
                                    CaseDescriptor caseDescriptor = new CaseDescriptor( caseEntity );

                                    final Transforms.Counter<Attachment> counter = new Transforms.Counter<Attachment>();
                                    final CaseEntity theCase = caseEntity;
                                    caseDescriptor.everyAttachmentOnCase().transferTo( Transforms.map( counter, new Output<Attachment, IOException>()
                                    {
                                        public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Attachment, SenderThrowableType> sender ) throws IOException, SenderThrowableType
                                        {
                                            sender.sendTo( new Receiver<Attachment, IOException>()
                                            {
                                                public void receive( Attachment attachment ) throws IOException
                                                {
                                                    String fileUri = ((AttachedFile.Data) attachment).uri().get();
                                                    try
                                                    {
                                                        final String fileId = new URI( fileUri ).getSchemeSpecificPart();
                                                        attachmentStore.attachment( fileId ).transferTo(
                                                                Outputs.byteBuffer(
                                                                        new File(archiveDir, theCase.caseId().get() + "_"
                                                                                + counter.getCount() + "_"
                                                                                + ((AttachedFile.Data) attachment).name().get())) );
                                                    } catch ( URISyntaxException se )
                                                    {
                                                        logger.error( "Uri is not valid! - " + fileUri );
                                                    } catch ( FileNotFoundException fnf )
                                                    {
                                                        logger.warn("File does not exist in attachment store! " + fnf.getMessage());
                                                    }

                                                }
                                            });
                                        }
                                    }));
                                }
                                caseEntity.deleteEntity();
                                toArchive++;
                            } catch (Throwable throwable)
                            {
                                logger.warn("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", could not be archived", throwable);
                            }
                        }
                    }
                }

            } catch ( Throwable t )
            {

            } finally
            {
                if(uow.isOpen())
                {
                    uow.complete();
                }
                RoleMap.clearCurrentRoleMap();

                DateTime end = new DateTime(  );
                Period elapsed = new Duration( start, end ).toPeriod();

                logger.info( "Archival ended: " + end.toString( "yyyy-MM-dd hh:mm:ss" ) + " and took "
                        + elapsed.getDays() + " days " + elapsed.getHours() + " hours "
                        + elapsed.getMinutes() + " minutes " + elapsed.getSeconds() + " seconds");
                logger.info( toArchive + forDelete + " Cases where processed." );
                logger.info( toArchive + " cases where removed and archived." );
                logger.info( forDelete + " cases where removed without archiving." );

                // reset interruption
                interruptRequest = false;

            }
        }



        private File exportPdf(CaseEntity caseEntity) throws Throwable
        {

            ValueBuilder<CaseOutputConfigDTO> builder = module.valueBuilderFactory().newValueBuilder(CaseOutputConfigDTO.class);
            builder.prototype().caselog().set(true);
            builder.prototype().attachments().set(true);
            builder.prototype().contacts().set(true);
            builder.prototype().conversations().set(true);
            builder.prototype().submittedForms().set(true);
            CaseOutputConfigDTO configOutput = builder.newInstance();

            final PDDocument pdf = pfdGenerator.generateCasePdf( caseEntity, configOutput, null );

            File file = new File(archiveDir, caseEntity.caseId().get()+".pdf");
            OutputStream out = null;
            try
            {
                out = new FileOutputStream(file);
                pdf.save(out);
                out.close();
                pdf.close();
                return file;
            } catch (IOException e)
            {
                if (out != null)
                    out.close();
                file.delete();
                throw e;
            } catch (COSVisitorException e)
            {
                if (out != null)
                    out.close();
                file.delete();
                throw e;
            } finally
            {
                pdf.close();
            }
        }

        // TODO Json export
        private File exportJson( CaseEntity caseEntity )
        {
            File file = new File(archiveDir, caseEntity.caseId().get()+".json");

            return file;
        }

        private Iterable<ArchivalSettings.Data> archivalSettings()
        {
            Property<Integer> maxAge = templateFor(ArchivalSettings.Data.class).archivalSettings().get().maxAge();
            Query<ArchivalSettings.Data> settings = module.queryBuilderFactory().newQueryBuilder(ArchivalSettings.Data.class).where(notEq(maxAge, 0)).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
            return settings;
        }

        private Iterable<CaseEntity> archivableCases(Iterable<ArchivalSettings.Data> settings)
        {
            return Iterables.flatten(Iterables.map(new Function<ArchivalSettings.Data, Iterable<CaseEntity>>()
            {
                public Iterable<CaseEntity> map(ArchivalSettings.Data setting)
                {
                    Date maxAgeDate = new DateTime().minusDays( setting.archivalSettings().get().maxAge().get() ).toDate();

                    QueryBuilder<CaseEntity> builder= module.queryBuilderFactory().
                            newQueryBuilder( CaseEntity.class );

                    if( setting instanceof LabelEntity)
                    {

                        builder = builder.where(and(contains(templateFor(Labelable.Data.class).labels(), (Label) setting),
                                or(eq(templateFor(Status.Data.class).status(), CaseStates.CLOSED),
                                        eq(templateFor(Removable.Data.class).removed(), Boolean.TRUE)),
                                lt(templateFor(CreatedOn.class).createdOn(), maxAgeDate)));

                    } else
                    {
                        builder = builder.where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                                or( eq( templateFor( Status.Data.class ).status(), CaseStates.CLOSED ),
                                        eq( templateFor( Removable.Data.class ).removed(), Boolean.TRUE ) ),
                                lt( templateFor( CreatedOn.class ).createdOn(), maxAgeDate ) ) );
                    }

                    return builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
                }
            }, settings));
        }


        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            try
            {
                logger.info("Start to archive cases");
                performArchival();
                logger.info("Finished archiving cases");
            } catch (Throwable e)
            {
                logger.error("Could not complete archiving cases", e);
            }
        }

        @Override
        public void interrupt() throws UnableToInterruptJobException
        {
            interruptRequest = true;
        }
    }
}
