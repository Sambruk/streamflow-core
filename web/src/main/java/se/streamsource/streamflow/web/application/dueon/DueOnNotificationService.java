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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
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
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

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
         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(dueOnCheck);
         try
         {
            List<DueOnNotification> notifications = createNotificationList();
            sendNotifications(notifications);
         } finally
         {
            uow.complete();
         }
      }

      private List<DueOnNotification> createNotificationList() throws UnitOfWorkCompletionException {
         
         Map<Contactable,DueOnNotification> contactablesMap = new HashMap<Contactable, DueOnNotification>();
         
         for (DueOnNotificationSettings.Data data : dueOnNotificationSettings())
         {
            final DueOnNotificationSettingsDTO settings = data.notificationSettings().get();
            if (settings.active().get())
            {
               for (CaseEntity caze : dueOnCases(data))
               {
                  if (caze.assignedTo().get() != null) {
                     getNotification((Contactable) caze.assignedTo().get(), contactablesMap).getPersonalOverdueCases().add( caze );
                  } else {
                     List<Contactable> recipients = resolveFunctionRecipients(((Members.Data)caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionOverdueCases().add( caze );
                     }
                  }
                  if (settings.additionalrecievers().get() != null && !settings.additionalrecievers().get().isEmpty()) {
                     for (EntityReference contactableRef : settings.additionalrecievers().get())
                     {
                        Contactable contactable = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, contactableRef.identity());
                        getNotification( contactable, contactablesMap).getMonitoredOverdueCases().add( caze );
                     }
                  }
               }
            }
         }

         for (DueOnNotificationSettings.Data data : dueOnNotificationSettings())
         {
            final DueOnNotificationSettingsDTO settings = data.notificationSettings().get();
            if (settings.active().get()) 
            {
               for (CaseEntity caze : dueOnThresholdCases(data))
               {
                  if (caze.assignedTo().get() != null) {
                     getNotification((Contactable) caze.assignedTo().get(), contactablesMap).getPersonalThresholdCases().add( caze );
                  } else {
                     List<Contactable> recipients = resolveFunctionRecipients(((Members.Data)caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionThresholdCases().add( caze );
                     }
                  }
                  if (settings.additionalrecievers().get() != null && !settings.additionalrecievers().get().isEmpty()) {
                     for (EntityReference contactableRef : settings.additionalrecievers().get())
                     {
                        Contactable contactable = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, contactableRef.identity());
                        getNotification( contactable, contactablesMap).getMonitoredThresholdCases().add( caze );
                     }
                  }
               }
            }
         }

         return new ArrayList<DueOnNotification>(contactablesMap.values());
      }

      private DueOnNotification getNotification(Contactable recipient, Map<Contactable,DueOnNotification> contactablesMap)
      {
         DueOnNotification notification = contactablesMap.get( recipient );
         if (notification == null)
         {
            notification = new DueOnNotification( recipient );
            contactablesMap.put( recipient, notification );
         }
         return notification;
      }

      private void sendNotifications(List<DueOnNotification> notifications)
      {
         for (DueOnNotification notification : notifications)
         {
           System.out.println("Notification to: " + notification.getRecipient().getContact().defaultEmail().emailAddress().get() + " =================");
           
           // Personal overdue cases
           if (!notification.getPersonalOverdueCases().isEmpty()) {
              System.out.println(" Overdue cases assigned to me: ");
           }
           for (CaseEntity caze : notification.getPersonalOverdueCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + " Duedate: " + caze.dueOn().get());
           }
           
           // Function overdue cases
           if (!notification.getFunctionOverdueCases().isEmpty()) {
              System.out.println(" Overdue cases assigned to one of my functions: ");
           }
           for (CaseEntity caze : notification.getFunctionOverdueCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + "  Duedate: " + caze.dueOn().get() +  "  Function: " + caze.owner().get());
           }
           
           // Monitored overdue cases
           if (!notification.getMonitoredOverdueCases().isEmpty()) {
              System.out.println(" Overdue cases that I monitor: ");
           }
           for (CaseEntity caze : notification.getMonitoredOverdueCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + "  Duedate: " + caze.dueOn().get());
           }
           
           // Personal threshold cases
           if (!notification.getPersonalThresholdCases().isEmpty()) {
              System.out.println(" Threshold Overdue cases assigned to me: ");
           }
           for (CaseEntity caze : notification.getPersonalThresholdCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + " Duedate: " + caze.dueOn().get());
           }
           
           // Function threshold cases
           if (!notification.getFunctionThresholdCases().isEmpty()) {
              System.out.println(" Threshold overdue cases assigned to one of my functions: ");
           }
           for (CaseEntity caze : notification.getFunctionThresholdCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + "  Duedate: " + caze.dueOn().get() +  "  Function: " + caze.owner().get());
           }
           
           // Monitored threshold cases
           if (!notification.getMonitoredThresholdCases().isEmpty()) {
              System.out.println(" Overdue cases that I monitor: ");
           }
           for (CaseEntity caze : notification.getMonitoredThresholdCases())
           {
              System.out.println("   CaseId: " +  caze.caseId().get() + "  Duedate: " + caze.dueOn().get());
           }
         }
      }
      
      private List<Contactable> resolveFunctionRecipients(List<Member> members)
      {
         List<Contactable> contacts = new ArrayList<Contactable>();
         for (Member member : members)
         {
            if (member instanceof Group){
               contacts.addAll(resolveGroupMembers(((Participants.Data) member).participants().toList()));
            } else {
               contacts.add((Contactable) member);
            }
         }
         return contacts;
      }
      
      private List<Contactable> resolveGroupMembers(List<Participant> participants)
      {
         List<Contactable> contacts = new ArrayList<Contactable>();
         for (Participant participant : participants)
         {
            if (participant instanceof Participants) {
               contacts.addAll(resolveGroupMembers( ((Participants.Data ) participant).participants().toList()));
            }
            contacts.add( (Contactable) participant );
         }
         return contacts;
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

      private Iterable<CaseEntity> dueOnCases(DueOnNotificationSettings.Data setting)
      {
        return module.queryBuilderFactory().
                  newQueryBuilder( CaseEntity.class ).
                  where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                              or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                    eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
                              lt( templateFor( DueOn.Data.class ).dueOn(), new DateTime().toDate() ) ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      }

      private Iterable<CaseEntity> dueOnThresholdCases(DueOnNotificationSettings.Data setting)
      {
         Date thresholdDate = new DateTime().plusDays( setting.notificationSettings().get().threshold().get() ).toDateTime( DateTimeZone.UTC ).toDateMidnight().toDate();

         return module.queryBuilderFactory().
               newQueryBuilder( CaseEntity.class ).
               where( and( eq( templateFor( TypedCase.Data.class ).caseType(), (CaseType) setting ),
                           or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                 eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
                           eq( templateFor( DueOn.Data.class ).dueOn(), thresholdDate ) ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      }   
   }
}
