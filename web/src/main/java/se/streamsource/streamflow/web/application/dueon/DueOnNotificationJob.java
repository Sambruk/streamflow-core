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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.administration.DueOnNotificationSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
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

@Mixins(DueOnNotificationJob.Mixin.class)
public interface DueOnNotificationJob extends MailSender, Job, TransientComposite
{
 
   public void performNotification() throws UnitOfWorkCompletionException;
   
   abstract class Mixin implements DueOnNotificationJob
   {
      @This
      DueOnNotificationJob task;

      @This
      private MailSender mailSender;

      @Structure
      Module module;

      Usecase dueOnCheck = UsecaseBuilder.newUsecase("DueOn Notification check");

      Logger logger = LoggerFactory.getLogger(DueOnNotificationService.class);

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
         
         for (CaseTypeEntity caseType : dueOnNotificationSettings())
         {
            final DueOnNotificationSettingsDTO settings = caseType.notificationSettings().get();
            if (settings.active().get())
            {
               for (CaseEntity caze : dueOnCases(caseType))
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

         for (DueOnNotificationSettings.Data data : dueOnThresholdNotificationSettings())
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
         UserEntity administrator = module.unitOfWorkFactory().currentUnitOfWork().get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);
         for (DueOnNotification notification : notifications)
         {                  
            ContactEmailDTO recipientEmail = notification.getRecipient().getContact().defaultEmail();
            if (recipientEmail != null)
            {
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);
               builder.prototype().fromName().set( administrator.getDescription() );
               try
               {
                  builder.prototype().from().set(administrator.contact().get().defaultEmail().emailAddress().get());
               } catch ( NullPointerException npe )
               {
                  throw new IllegalStateException( "Mail notification failed! Administrator mail address is not set." );
               }

               builder.prototype().to().set( recipientEmail.emailAddress().get() );
               builder.prototype().subject().set( "Streamflow updates");
               builder.prototype().content().set( createTextFormatedReport(notification) );
               builder.prototype().contentType().set( "text/plain" );
           
               mailSender.sentEmail( null, builder.newInstance() );
            }
         }
      }
      
      private String createTextFormatedReport(DueOnNotification notification)
      {
         StringBuffer message = new StringBuffer();
         // Personal overdue cases
         if (!notification.getPersonalOverdueCases().isEmpty()) {
            message.append("== Overdue cases assigned to me:\r\n");
         }
         for (CaseEntity caze : notification.getPersonalOverdueCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append(" Duedate: ").append( caze.dueOn().get()).append( "\r\n" );
         }
         
         // Function overdue cases
         if (!notification.getFunctionOverdueCases().isEmpty()) {
            message.append("== Overdue cases assigned to one of my functions:\r\n");
         }
         for (CaseEntity caze : notification.getFunctionOverdueCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append("  Duedate: ").append( caze.dueOn().get()).append("  Function: ").append( ((Describable)caze.owner().get()).getDescription()).append( "\r\n" );
         }
         
         // Monitored overdue cases
         if (!notification.getMonitoredOverdueCases().isEmpty()) {
            message.append("== Overdue cases that I monitor:\r\n");
         }
         for (CaseEntity caze : notification.getMonitoredOverdueCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append("  Duedate: ").append( caze.dueOn().get()).append( "\r\n" );
         }
         
         // Personal threshold cases
         if (!notification.getPersonalThresholdCases().isEmpty()) {
            message.append("== Threshold Overdue cases assigned to me:\r\n");
         }
         for (CaseEntity caze : notification.getPersonalThresholdCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append(" Duedate: ").append( caze.dueOn().get()).append( "\r\n" );
         }
         
         // Function threshold cases
         if (!notification.getFunctionThresholdCases().isEmpty()) {
            message.append("== Threshold overdue cases assigned to one of my functions:\r\n");
         }
         for (CaseEntity caze : notification.getFunctionThresholdCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append("  Duedate: ").append( caze.dueOn().get()).append("  Function: ").append( ((Describable)caze.owner().get()).getDescription()).append( "\r\n" );
         }
         
         // Monitored threshold cases
         if (!notification.getMonitoredThresholdCases().isEmpty()) {
            message.append(" Overdue cases that I monitor:\r\n");
         }
         for (CaseEntity caze : notification.getMonitoredThresholdCases())
         {
            message.append("   CaseId: ").append( caze.caseId().get()).append("  Duedate: ").append( caze.dueOn().get()).append( "\r\n" );
         }
         return message.toString();
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

      private Iterable<CaseTypeEntity> dueOnNotificationSettings()
      {
         Property<Boolean> active = templateFor(DueOnNotificationSettings.Data.class).notificationSettings().get().active();
         Query<CaseTypeEntity> settings = module.queryBuilderFactory().newQueryBuilder(CaseTypeEntity.class).where(eq(active, true)).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
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




      @Override
      public void execute(JobExecutionContext context) throws JobExecutionException
      {
         try
         {
            logger.info("Start to send due on notifications");
            performNotification();
            logger.info("Finished sending due on notifications");
         } catch (Throwable e)
         {
            logger.error("Could not complete sending due on notifications", e);
         }
         
      }   
   }
}
