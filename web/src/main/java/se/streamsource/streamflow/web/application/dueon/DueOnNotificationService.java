/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.application.dueon;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.notEq;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
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
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.administration.DueOnNotificationSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.DueOnNotificationSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

/**
 * TODO
 */
@Mixins(DueOnNotificationService.Mixin.class)
public interface DueOnNotificationService
      extends ServiceComposite, Configuration<DueOnNotificationConfiguration>, Activatable
{

   public void performNotification() throws UnitOfWorkCompletionException;

   abstract class Mixin
         implements DueOnNotificationService, Activatable, Runnable
   {
      @Service
      PdfGeneratorService pfdGenerator;

      @This
      Configuration<DueOnNotificationConfiguration> config;

      @Structure
      Module module;

      Usecase dueOnCheck = UsecaseBuilder.newUsecase("DueOn Notification check");

      Logger logger = LoggerFactory.getLogger(DueOnNotificationService.class);
      private ScheduledExecutorService dailyChecker;

      public void activate() throws Exception
      {
         // TODO Start the scheduler
      }

      public void passivate() throws Exception
      {
         // TODO Stop the scheduler
      }

      public void run()
      {
         try
         {
            logger.info("Starting daily archival");
            performNotification();
            logger.info("Finished daily archival");
         } catch (Throwable e)
         {
            logger.error("Could not complete daily archival", e);
         }
      }


      public void performNotification() throws UnitOfWorkCompletionException
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(dueOnCheck);

         try
         {
            for (DueOnNotificationSettings.Data data : dueOnNotificationSettings())
            {
               DueOnNotificationSettingsDTO settings = data.notificationSettings().get();
               for (CaseEntity caseEntity : dueOnCases(Iterables.iterable(data)))
               {
                  try
                  {
                     
                     logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), due on " + caseEntity.dueOn().get() + " has passed due on date.");
                     //caseEntity.deleteEntity();
                  } catch (Exception e)
                  {
                     logger.warn("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), due on " + caseEntity.dueOn().get() + ", could not notify user", e);
                  }
               }
            }

            for (DueOnNotificationSettings.Data data : dueOnThresholdNotificationSettings())
            {
               DueOnNotificationSettingsDTO settings = data.notificationSettings().get();
               for (CaseEntity caseEntity : dueOnThresholdCases(Iterables.iterable(data), settings.threshold().get()))
               {
                  try
                  {
                     
                     logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), due on " + caseEntity.dueOn().get() + " will passed due on date in " + settings.threshold().get() + " days");
                     //caseEntity.deleteEntity();
                  } catch (Exception e)
                  {
                     logger.warn("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), due on " + caseEntity.dueOn().get() + ", could not notify user that its about to pass due on date", e);
                  }
               }
            }
         } finally
         {
            uow.complete();
         }
      }

      private Iterable<DueOnNotificationSettings.Data> dueOnNotificationSettings()
      {
         Property<Boolean> active = templateFor(DueOnNotificationSettings.Data.class).notificationSettings().get().active();
         Query<DueOnNotificationSettings.Data> settings = module.queryBuilderFactory().newQueryBuilder(DueOnNotificationSettings.Data.class).where(eq(active, true)).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
         return settings;
      }
      
      private Iterable<DueOnNotificationSettings.Data> dueOnThresholdNotificationSettings()
      {
         Property<Boolean> active = templateFor(DueOnNotificationSettings.Data.class).notificationSettings().get().active();
         Property<Integer> threshold = templateFor(DueOnNotificationSettings.Data.class).notificationSettings().get().threshold();
         Query<DueOnNotificationSettings.Data> settings = module.queryBuilderFactory().newQueryBuilder(DueOnNotificationSettings.Data.class).where(and( eq(active, true), notEq(threshold, 0))).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
         return settings;
      }

      private Iterable<CaseEntity> dueOnCases(Iterable<DueOnNotificationSettings.Data> settings)
      {
         return Iterables.flatten(Iterables.map(new Function<DueOnNotificationSettings.Data, Iterable<CaseEntity>>()
         {
            public Iterable<CaseEntity> map(DueOnNotificationSettings.Data setting)
            {

               Query<CaseEntity> cases = module.queryBuilderFactory().
                     newQueryBuilder( CaseEntity.class ).
                     where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                                 or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                       eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
                                 lt( templateFor( DueOn.Data.class ).dueOn(), new DateTime().toDate() ) ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
               return cases;
            }
         }, settings));
      }

      private Iterable<CaseEntity> dueOnThresholdCases(Iterable<DueOnNotificationSettings.Data> settings, final Integer threshold)
      {
         return Iterables.flatten(Iterables.map(new Function<DueOnNotificationSettings.Data, Iterable<CaseEntity>>()
         {
            public Iterable<CaseEntity> map(DueOnNotificationSettings.Data setting)
            {
               Date thresholdDate = new DateTime().plusDays( threshold ).toDateTime( DateTimeZone.UTC ).toDateMidnight().toDate();

               Query<CaseEntity> cases = module.queryBuilderFactory().
                     newQueryBuilder( CaseEntity.class ).
                     where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                                 or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                       eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
                                 eq( templateFor( DueOn.Data.class ).dueOn(), thresholdDate ) ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

               return cases;
            }
         }, settings));
      }   
   }
}
