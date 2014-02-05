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
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
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
import se.streamsource.streamflow.web.domain.entity.caze.CaseDescriptor;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.casetype.ArchivalSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * TODO
 */
@Mixins(ArchivalService.Mixin.class)
public interface ArchivalService
      extends ServiceComposite, Configuration<ArchivalConfiguration>, Activatable
{
   String performArchivalCheck();

   public void performArchival() throws UnitOfWorkCompletionException;

   abstract class Mixin
         implements ArchivalService, Activatable, Runnable
   {
      @Service
      FileConfiguration fileConfiguration;

      @Service
      PdfGeneratorService pfdGenerator;

      @Service
      AttachmentStoreService attachmentStore;

      @This
      Configuration<ArchivalConfiguration> config;

      File archiveDir;

      @Structure
      Module module;

      Usecase archivalCheck = UsecaseBuilder.newUsecase("Archival check");

      Logger logger = LoggerFactory.getLogger(ArchivalService.class);
      private ScheduledExecutorService dailyChecker;

      public void activate() throws Exception
      {
         archiveDir = new File(fileConfiguration.dataDirectory(), "archive");
         archiveDir.mkdir();

         if (config.configuration().archiveDaily().get())
         {
            // Start daily checker
            dailyChecker = Executors.newSingleThreadScheduledExecutor();
            dailyChecker.scheduleAtFixedRate(this, 0, 1, TimeUnit.DAYS);
         }
      }

      public void passivate() throws Exception
      {
         if (dailyChecker != null)
         {
            dailyChecker.shutdown();
         }
      }

      public void run()
      {
         try
         {
            logger.info("Starting daily archival");
            performArchival();
            logger.info("Finished daily archival");
         } catch (Throwable e)
         {
            logger.error("Could not complete daily archival", e);
         }
      }

      public String performArchivalCheck()
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(archivalCheck);
         int toArchive = 0;
         int markedForDelete = 0;
         try
         {
            for (CaseEntity caseEntity : archivableCases(archivalSettings()))
            {
               CaseType caseType = caseEntity.caseType().get();
               if( ((Removable.Data)caseEntity).removed().get() )
               {
                  logger.info( "Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + (caseType == null ? "" : ", "+caseType.getDescription())+"), created on " + caseEntity.createdOn().get() + ",is marked for remove and can be deleted" );
                  markedForDelete++;
               } else
               {
                  logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + (caseType == null ? "" : ", "+caseType.getDescription())+"), created on " + caseEntity.createdOn().get() + ", can be archived");
                  toArchive++;
               }
            }
         } finally
         {
            uow.discard();
         }

         return "" + toArchive + " cases can be archived.\r\n" + markedForDelete + " cases are marked for delete.";
      }

      public void performArchival() throws UnitOfWorkCompletionException
      {
         DateTime start = new DateTime( );
         logger.info( "Archival started: " + start.toString( "yyyy-MM-dd hh:mm:ss" ) );
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(archivalCheck);

         RoleMap.newCurrentRoleMap();
         RoleMap.current().set( uow.get( UserAuthentication.class, UserEntity.ADMINISTRATOR_USERNAME ) );
         RoleMap.current().set( new UserPrincipal( UserEntity.ADMINISTRATOR_USERNAME ) );

         try
         {
            for (ArchivalSettings.Data data : archivalSettings())
            {
               ArchivalSettingsDTO settings = data.archivalSettings().get();
               for (CaseEntity caseEntity : archivableCases(Iterables.iterable(data)))
               {
                  if (settings.archivalType().get().equals(ArchivalSettingsDTO.ArchivalType.delete))
                  {
                     try
                     {
                        logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", was deleted");
                        caseEntity.deleteEntity();
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
                           logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", was archived");

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
                                                   new File( archiveDir, theCase.caseId().get() + "_"
                                                         + counter.getCount() + "_"
                                                         + ((AttachedFile.Data) attachment).name().get() ) ) );
                                       } catch ( URISyntaxException se )
                                       {
                                          logger.error( "Uri is not valid! - " + fileUri );
                                       } catch ( FileNotFoundException fnf )
                                       {
                                          logger.info( "File does not exist in attachment store! " + fnf.getMessage() );
                                       }

                                    }
                                 });
                              }
                           }));
                        }
                        caseEntity.deleteEntity();
                     } catch (Throwable throwable)
                     {
                        logger.warn("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", could not be archived", throwable);
                     }
                  }
               }
            }

         } finally
         {
            uow.complete();
            RoleMap.clearCurrentRoleMap();

            DateTime end = new DateTime(  );
            Period elapsed = new Duration( start, end ).toPeriod();

            logger.info( "Archival ended: " + end.toString( "yyyy-MM-dd hh:mm:ss" ) + " and took "
                  + elapsed.getDays() + " days " + elapsed.getHours() + " hours "
            + elapsed.getMinutes() + " minutes " + elapsed.getSeconds() + " seconds");

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

               Query<CaseEntity> cases = module.queryBuilderFactory().
                     newQueryBuilder( CaseEntity.class ).
                     where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                                 or( eq( templateFor( Status.Data.class ).status(), CaseStates.CLOSED ),
                                       eq( templateFor( Removable.Data.class ).removed(), Boolean.TRUE ) ),
                                 lt( templateFor( CreatedOn.class ).createdOn(), maxAgeDate ) ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

               return cases;
            }
         }, settings));
      }
   }
}
