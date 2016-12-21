/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.dueon;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
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
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.DueOnNotificationSettings;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.qi4j.api.query.QueryExpressions.*;

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

      @Service
      VelocityEngine velocity;
      
      Usecase dueOnCheck = UsecaseBuilder.newUsecase("DueOn Notification check");

      Logger logger = LoggerFactory.getLogger(DueOnNotificationService.class);

      // Force swedish until we have locale support in user profile...
      Locale locale = new Locale("SV", "se");

      private ResourceBundle bundle = ResourceBundle.getBundle( DueOnNotificationJob.class.getName(), locale );
      
      public void performNotification() throws UnitOfWorkCompletionException
      {
         logger.info( "Performing due on notifictation" );
         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(dueOnCheck);
         try
         {
            List<DueOnNotification> notifications = createNotificationList();
            sendNotifications(notifications);
         } finally
         {
            uow.complete();
            logger.info( "Due on notification done." );
         }
      }

      private List<DueOnNotification> createNotificationList() throws UnitOfWorkCompletionException {
         
         Map<Contactable,DueOnNotification> contactablesMap = new HashMap<Contactable, DueOnNotification>();
         
         for (ProjectEntity caseType : dueOnNotificationSettings())
         {
            final DueOnNotificationSettingsDTO settings = caseType.notificationSettings().get();
            if (settings.active().get())
            {
               for (CaseEntity caze : dueOnCases(caseType))
               {
                  if (caze.assignedTo().get() != null) {
                     getNotification((Contactable) caze.assignedTo().get(), contactablesMap).getPersonalOverdueCases().add( new DueOnItem(caze, locale) );
                  } else {
                     List<Contactable> recipients = resolveRecipients(((Members.Data) caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionOverdueCases().add( new DueOnItem(caze, locale)  );
                     }
                  }
                  if (settings.additionalrecipients().get() != null && !settings.additionalrecipients().get().isEmpty()) {
                      List<Contactable> recipients = resolveAdditionalRecipients(contactablesMap, settings, caze);
                      for (Contactable recipient : recipients)
                      {
                          getNotification(recipient, contactablesMap).getFunctionOverdueCases().add( new DueOnItem(caze, locale)  );
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
                     getNotification((Contactable) caze.assignedTo().get(), contactablesMap).getPersonalThresholdCases().add( new DueOnItem(caze, locale)  );
                  } else {
                     List<Contactable> recipients = resolveRecipients(((Members.Data) caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionThresholdCases().add( new DueOnItem(caze, locale)  );
                     }
                  }
                  if (settings.additionalrecipients().get() != null && !settings.additionalrecipients().get().isEmpty()) {
                      List<Contactable> recipients = resolveAdditionalRecipients(contactablesMap, settings, caze);
                      for (Contactable recipient : recipients)
                      {
                          getNotification(recipient, contactablesMap).getFunctionThresholdCases().add( new DueOnItem(caze, locale)  );
                      }
                  }
               }
            }
         }

         return new ArrayList<DueOnNotification>(contactablesMap.values());
      }

       private List<Contactable> resolveAdditionalRecipients(Map<Contactable, DueOnNotification> contactablesMap, DueOnNotificationSettingsDTO settings, CaseEntity caze) {
           List<Member> members = new ArrayList<Member>();
           for (EntityReference contactableRef : settings.additionalrecipients().get())
           {
               members.add(module.unitOfWorkFactory().currentUnitOfWork().get(Member.class, contactableRef.identity()));
           }

          return resolveRecipients( members );
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
         HtmlMailGenerator mailGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
         for (DueOnNotification notification : notifications)
         {
            VelocityContext context = new VelocityContext();
            context.put( "user", notification.getRecipient().getContact().name().get() );
            context.put( "notification", notification );
            context.put( "today", Strings.capitalize( (new SimpleDateFormat( "d':e' MMMM", locale )).format( new Date() )));

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
               builder.prototype().subject().set( bundle.getString( "mail.title" ));
               builder.prototype().contentType().set( Translator.HTML );
               builder.prototype().content().set( mailGenerator.createDueOnNotificationMail( context ) );

               mailSender.sentEmail( null, builder.newInstance() );
            }
         }
      }

      private List<Contactable> resolveRecipients(List<Member> members)
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

      private Iterable<ProjectEntity> dueOnNotificationSettings()
      {
         Property<Boolean> active = templateFor(DueOnNotificationSettings.Data.class).notificationSettings().get().active();
         Query<ProjectEntity> settings = module.queryBuilderFactory().newQueryBuilder(ProjectEntity.class).where(eq(active, true)).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
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
                  where( and( eq( templateFor( Ownable.Data.class ).owner(), (Project) setting ),
                              eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                              eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ),
                              lt( templateFor( DueOn.Data.class ).dueOn(), new DateTime().toDate() ) ) )
                  .newQuery( module.unitOfWorkFactory().currentUnitOfWork() )
                  .orderBy( QueryExpressions.orderBy( QueryExpressions.templateFor( DueOn.Data.class ).dueOn(), OrderBy.Order.ASCENDING ) );
      }

      private Iterable<CaseEntity> dueOnThresholdCases(DueOnNotificationSettings.Data setting)
      {
         Date thresholdDate = new DateTime().plusDays( setting.notificationSettings().get().threshold().get() ).toDateTime( DateTimeZone.UTC ).toDateMidnight().toDate();

         return module.queryBuilderFactory().
               newQueryBuilder( CaseEntity.class ).
               where( and( eq( templateFor( Ownable.Data.class ).owner(), (Project) setting ),
                           eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                           eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ),
                           and(le( templateFor( DueOn.Data.class ).dueOn(), thresholdDate ),
                              gt( templateFor( DueOn.Data.class ).dueOn(), new DateTime().toDate() ))) )
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() )
               .orderBy( QueryExpressions.orderBy( QueryExpressions.templateFor( DueOn.Data.class ).dueOn(), OrderBy.Order.ASCENDING ) );
      }


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
