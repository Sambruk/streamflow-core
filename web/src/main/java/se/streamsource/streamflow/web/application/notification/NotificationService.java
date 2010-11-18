/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.application.notification;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.event.application.factory.ApplicationEventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTracker;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CompletableId;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationOwner;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageReceiver;

/**
 * Send and receive notifications. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a notification to the provided recipient.
 */
@Mixins(NotificationService.Mixin.class)
@Concerns(ApplicationEventCreationConcern.class)
public interface NotificationService
      extends Configuration, Activatable, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      final Logger logger = LoggerFactory.getLogger( NotificationService.class.getName() );

      @Service
      private EventSource eventSource;

      @Service
      private EventStream stream;

      @Structure
      private UnitOfWorkFactory uowf;

      @Structure
      private ValueBuilderFactory vbf;

      @This
      private Configuration<NotificationConfiguration> config;

      @This
      private MailSender mailSender;

      private TransactionTracker tracker;

      @Service
      DomainEventPlayer player;

      private SendEmails sendEmails = new SendEmails();

      public void activate() throws Exception
      {
         EventRouter router = new EventRouter();
         router.route( Events.withNames( SendEmails.class ), Events.playEvents( player, sendEmails, uowf, UsecaseBuilder.newUsecase("Send email to participant" )) );

         tracker = new TransactionTracker( stream, eventSource, config, Events.adapter( router ) );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public class SendEmails
            implements MessageReceiver.Data
      {
         public void receivedMessage( DomainEvent event, Message message )
         {
            UnitOfWork uow = uowf.currentUnitOfWork();

            Message.Data messageData = (Message.Data) message;

            Conversation conversation = messageData.conversation().get();
            ConversationOwner owner = conversation.conversationOwner().get();

            String sender = ((Contactable.Data) messageData.sender().get()).contact().get().name().get();
            String caseId = "n/a";

            if (owner != null)
               caseId = ((CompletableId.Data) owner).caseId().get() != null ? ((CompletableId.Data) owner).caseId().get() : "n/a";

            UserEntity user = uow.get( UserEntity.class, event.entity().get() );

            if (user.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email ))
            {
               String subject = "[" + caseId + "]" + conversation.getDescription()
                     + "(" + messageData.conversation().get().toString() + ":" + event.entity().get() + ")";

               String formattedMsg = messageData.body().get();
               if (formattedMsg.contains( "<body>" ))
               {
                  formattedMsg = formattedMsg.replace( "<body>", "<body><b>" + sender + ":</b><br/><br/>" );
               } else
               {
                  formattedMsg = sender + ":\r\n\r\n" + formattedMsg;
               }

               ContactEmailValue recipientEmail = user.contact().get().defaultEmail();
               if (recipientEmail != null)
               {
                  ValueBuilder<EmailValue> builder = vbf.newValueBuilder( EmailValue.class );
   //                  builder.prototype().from().set( );
                  builder.prototype().replyTo();
                  builder.prototype().to().set( recipientEmail.emailAddress().get() );
                  builder.prototype().subject().set( subject );
                  builder.prototype().content().set( formattedMsg );
                  builder.prototype().contentType().set( "text/html" );

                  EmailValue emailValue = builder.newInstance();

                  mailSender.sentEmail( null, emailValue );
               }
            }
         }
      }
   }
}