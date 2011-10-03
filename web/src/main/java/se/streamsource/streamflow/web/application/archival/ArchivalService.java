/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
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
import se.streamsource.streamflow.api.administration.ArchivalSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.application.pdf.CasePdfGenerator;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.ArchivalSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
   void performArchivalCheck();

   public void performArchival() throws UnitOfWorkCompletionException;

   abstract class Mixin
         implements ArchivalService, Activatable, Runnable
   {
      @Service
      FileConfiguration fileConfiguration;

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

      public void performArchivalCheck()
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(archivalCheck);

         try
         {
            for (CaseEntity caseEntity : archivableCases(archivalSettings()))
            {
               CaseType caseType = caseEntity.caseType().get();
               logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + (caseType == null ? "" : ", "+caseType.getDescription())+"), created on " + caseEntity.createdOn().get() + ", can be archived");
            }
         } finally
         {
            uow.discard();
         }
      }

      public void performArchival() throws UnitOfWorkCompletionException
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(archivalCheck);

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
                        File pdf = exportPdf(caseEntity);
                        logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", was archived");
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
         }
      }

      private File exportPdf(CaseEntity caseEntity) throws Throwable
      {
         Ownable.Data project = (Ownable.Data) caseEntity.owner().get();
         Owner ou = project.owner().get();

         Organization org = ((OwningOrganization) ou).organization().get();

         AttachedFile.Data template = (AttachedFile.Data) ((CasePdfTemplate.Data) org).casePdfTemplate().get();

         if (template == null)
         {
            template = (AttachedFile.Data) ((DefaultPdfTemplate.Data) org).defaultPdfTemplate().get();
         }

         String uri = null;
         if (template != null)
         {
            uri = template.uri().get();
         }

         ValueBuilder<CaseOutputConfigDTO> builder = module.valueBuilderFactory().newValueBuilder(CaseOutputConfigDTO.class);
         builder.prototype().history().set(true);
         builder.prototype().attachments().set(true);
         builder.prototype().contacts().set(true);
         builder.prototype().conversations().set(true);
         builder.prototype().submittedForms().set(true);
         CaseOutputConfigDTO configOutput = builder.newInstance();

         CasePdfGenerator exporter = module.objectBuilderFactory().newObjectBuilder( CasePdfGenerator.class ).use( configOutput, uri, Locale.ENGLISH ).newInstance();

         caseEntity.outputCase(exporter);

         final PDDocument pdf = exporter.getPdf();

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
               Calendar calendar = Calendar.getInstance();
               calendar.add(Calendar.DAY_OF_MONTH, -setting.archivalSettings().get().maxAge().get());
               Date maxAgeDate = calendar.getTime();

               Query<CaseEntity> cases = module.queryBuilderFactory().
                     newQueryBuilder(CaseEntity.class).
                     where(and(QueryExpressions.eq(templateFor(TypedCase.Data.class).caseType(), (CaseType) setting),
                               eq(templateFor(Status.Data.class).status(), CaseStates.CLOSED),
                               lt(QueryExpressions.templateFor(CreatedOn.class).createdOn(), maxAgeDate))).newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               return cases;
            }
         }, settings));
      }
   }
}
