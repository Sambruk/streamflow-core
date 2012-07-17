/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.le;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.notEq;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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
import se.streamsource.streamflow.web.application.mail.EmailValue;
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
                     List<Contactable> recipients = resolveFunctionRecipients(((Members.Data)caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionOverdueCases().add( new DueOnItem(caze, locale)  );
                     }
                  }
                  if (settings.additionalrecipients().get() != null && !settings.additionalrecipients().get().isEmpty()) {
                     for (EntityReference contactableRef : settings.additionalrecipients().get())
                     {
                        Contactable contactable = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, contactableRef.identity());
                        getNotification( contactable, contactablesMap).getMonitoredOverdueCases().add( new DueOnItem(caze, locale)  );
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
                     List<Contactable> recipients = resolveFunctionRecipients(((Members.Data)caze.owner().get()).members().toList());
                     for (Contactable recipient : recipients)
                     {
                        getNotification(recipient, contactablesMap).getFunctionThresholdCases().add( new DueOnItem(caze, locale)  );
                     }
                  }
                  if (settings.additionalrecipients().get() != null && !settings.additionalrecipients().get().isEmpty()) {
                     for (EntityReference contactableRef : settings.additionalrecipients().get())
                     {
                        Contactable contactable = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, contactableRef.identity());
                        getNotification( contactable, contactablesMap).getMonitoredThresholdCases().add( new DueOnItem(caze, locale)  );
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
               builder.prototype().subject().set( bundle.getString( "mail.title" ));
               builder.prototype().content().set( createFormatedReport(notification,"dueonnotificationtextmail_sv.html") );
               builder.prototype().contentType().set( "text/plain" );
               builder.prototype().contentHtml().set( createFormatedReport(notification,"dueonnotificationhtmlmail_sv.html") );

               mailSender.sentEmail( null, builder.newInstance() );
            }
         }
      }
      
      private String createFormatedReport(DueOnNotification notification, String template)
      {
         VelocityContext context = new VelocityContext();
         context.put( "user", notification.getRecipient().getContact().name().get() );
         context.put( "notification", notification );
         context.put( "today", Strings.capitalize( (new SimpleDateFormat( "d':e' MMMM", locale )).format( new Date() )));
         StringWriter writer = new StringWriter();
         try
         {
            velocity.evaluate(context, writer, "dueonnotificationmail", getTemplate( template, getClass() ));

            return writer.toString();
         } catch (IOException e)
         {
            throw new IllegalArgumentException("Could not create html mail", e);
         }
      }
      
      public static String getTemplate(String resourceName, Class resourceClass) throws IOException
      {
         StringBuilder template = new StringBuilder( "" );
         InputStream in = resourceClass.getResourceAsStream( resourceName );
         BufferedReader reader = new BufferedReader( new InputStreamReader( in, "UTF-8") );
         String line;
         while ((line = reader.readLine()) != null)
            template.append( line + "\n" );
         reader.close();

         return template.toString();
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
                              or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                    eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
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
                           or( eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
                                 eq( templateFor( Removable.Data.class ).removed(), Boolean.FALSE ) ),
                           and(le( templateFor( DueOn.Data.class ).dueOn(), thresholdDate ),
                              gt( templateFor( DueOn.Data.class ).dueOn(), new DateTime().toDate() ))) )
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() )
               .orderBy( QueryExpressions.orderBy( QueryExpressions.templateFor( DueOn.Data.class ).dueOn(), OrderBy.Order.ASCENDING ) );
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
