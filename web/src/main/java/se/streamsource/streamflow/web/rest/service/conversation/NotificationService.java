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
package se.streamsource.streamflow.web.rest.service.conversation;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTracker;
import se.streamsource.streamflow.util.MessageTemplate;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.profile.MailFooter;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessRestriction;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.caze.Origin;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationOwner;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageReceiver;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.EmailTemplates;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Send and receive notifications. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a notification to the provided recipient.
 */
@Mixins(NotificationService.Mixin.class)
public interface NotificationService
      extends Configuration, Activatable, MailSender, ServiceComposite
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
      private Module module;

      @This
      private Configuration<NotificationConfiguration> config;

      @This
      private MailSender mailSender;

      private TransactionTracker tracker;

      @Service
      DomainEventPlayer player;

      private SendEmails sendEmails = new SendEmails();

      Map<String, String> templateDefaults = new HashMap<String, String>();
      private EmailAccessPoints eap;

      public void activate() throws Exception
      {
         // Get defaults for emails
         ResourceBundle bundle = ResourceBundle.getBundle(EmailTemplates.class.getName());
         for (String key : bundle.keySet())
         {
            templateDefaults.put(key, bundle.getString(key));
         }

         // Store reference to EAP
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Update templates"));
         eap = module.queryBuilderFactory().newQueryBuilder(EmailAccessPoints.class).newQuery(uow).find();
         uow.discard();

         EventRouter router = new EventRouter();
         router.route( Events.withNames( SendEmails.class ), Events.playEvents( player, sendEmails, module.unitOfWorkFactory(), UsecaseBuilder.newUsecase("Send email to participant" )) );

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
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            try
            {
               MessageReceiver recipient = uow.get( MessageReceiver.class, event.entity().get() );

               if (shouldSendFullMail(message, recipient))
               {
                  EmailValue emailValue = buildFullMail(message, recipient);
                  mailSender.sentEmail( null, emailValue );
               }
               else if (shouldSendNotificationMail(message, recipient))
               {
                  EmailValue emailValue = buildNotificationMail(message, recipient);
                  mailSender.sentEmail( null, emailValue );
               }
            } catch (Throwable e)
            {
               logger.error("Could not send notification to user entity = " +  event.entity().get() , e);
            }
         }

         private EmailValue buildFullMail(Message message, MessageReceiver recipient) throws UnsupportedEncodingException
         {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();

            ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

            builder.prototype().fromName().set( determineFromName(message) );
            builder.prototype().to().set( determineRecipientEmailAddress(recipient) );
            builder.prototype().subject().set( determineFullSubject(message) );
            builder.prototype().content().set( createFullHTMLMailContent(message) );
            builder.prototype().contentType().set( Translator.HTML );

            addAttachmentsToBuilder(message, builder);
            addEmailAccessPointHeadersToBuilder(message, builder);
            addThreadingHeadersToBuilder(conversation, recipient, builder);

            EmailValue emailValue = builder.newInstance();
            return emailValue;
         }

         private EmailValue buildNotificationMail(Message message, MessageReceiver recipient) throws UnsupportedEncodingException
         {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();

            ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

            builder.prototype().fromName().set( determineFromName(message) );
            builder.prototype().to().set( determineRecipientEmailAddress(recipient) );
            builder.prototype().subject().set( determineNotificationSubject(message) );
            builder.prototype().content().set( createNotificationHTMLMailContent(message) );
            builder.prototype().contentType().set( Translator.HTML );

            addEmailAccessPointHeadersToBuilder(message, builder);
            addThreadingHeadersToBuilder(conversation, recipient, builder);

            EmailValue emailValue = builder.newInstance();
            return emailValue;
         }

         private String determineFromName(Message message) {
            // check if sender is administrator and in that case dont set fromName - this will be picked up by
            // MailSender and replaced with the configurated default fromName
            Message.Data messageData = (Message.Data) message;
            String fromName = null;
            if( ! EntityReference.getEntityReference( messageData.sender().get() ).identity().equals( UserEntity.ADMINISTRATOR_USERNAME ))
            {
               fromName = ((Contactable.Data) messageData.sender().get()).contact().get().name().get();
            }
            return fromName;
         }

         private String determineFooter(Message message) {
            Message.Data messageData = (Message.Data) message;
            String footer ="";
            if( messageData.sender().get() instanceof MailFooter)
            {
               footer = ((MailFooter.Data)messageData.sender().get()).footer().get();
            }
            return footer;
         }

         private String determineCaseId(ConversationOwner owner) {
            String caseId = "n/a";

            if (owner != null)
               caseId = ((CaseId.Data) owner).caseId().get() != null ? ((CaseId.Data) owner).caseId().get() : "n/a";
            return caseId;
         }

         private String determineRecipientEmailAddress(MessageReceiver recipient) {
            ContactEmailDTO recipientEmail = ((Contactable.Data)recipient).contact().get().defaultEmail();
            if (recipientEmail == null) {
               return null;
            }
            else {
               return recipientEmail.emailAddress().get();
            }

         }

         private String createFullHTMLMailContent(Message message) {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            Origin origin = (Origin) conversationOwner;
            EmailAccessPoint emailAccessPoint = origin.accesspoint().get();

            String formattedMsg;
            {
               if (emailAccessPoint != null)
               {
                  formattedMsg = message.translateBody(emailAccessPoint.emailTemplates().get());
               } else
               {
                  formattedMsg = message.translateBody(templateDefaults);
               }
            }

            if( messageData.messageType().get().equals( MessageType.PLAIN ) ||
                  messageData.messageType().get().equals( MessageType.SYSTEM ))
            {
               StringBuffer buf = new StringBuffer(  );
               Scanner scanner = new Scanner( formattedMsg );
               while( scanner.hasNextLine() )
               {
                  buf.append( scanner.nextLine() + "<BR>" + System.getProperty( "line.separator" ) );

               }
               scanner.close();
               formattedMsg = buf.toString();
            }

            HtmlMailGenerator htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
            String mailContent = htmlGenerator.createMailContent( formattedMsg, determineFooter(message) );
            return mailContent;
         }

         private String createNotificationHTMLMailContent(Message message) {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            String caseId = determineCaseId(conversationOwner);

            String mailBody = config.configuration().notificationOnlyMailBody().get();
            String formattedMsg = String.format(mailBody, caseId);

            HtmlMailGenerator htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
            String mailContent = htmlGenerator.createMailContent( formattedMsg, determineFooter(message) );
            return mailContent;
         }

         private String determineFullSubject(Message message) {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            Origin origin = (Origin) conversationOwner;
            EmailAccessPoint emailAccessPoint = origin.accesspoint().get();

            String subject;
            {
               String caseId = determineCaseId(conversationOwner);
               if (emailAccessPoint != null)
               {
                  subject = MessageTemplate.text(emailAccessPoint.subject().get()).bind("caseid", caseId).bind("subject", conversation.getDescription()).eval();
               } else
               {
                  subject = "[" + caseId + "] " + conversation.getDescription(); // Default subject format
               }
            }
            return subject;
         }

         private String determineNotificationSubject(Message message) {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            String caseId = determineCaseId(conversationOwner);
            String subjectText = config.configuration().notificationOnlyMailSubject().get();
            return  "[" + caseId + "] " + subjectText;
         }

         private void addEmailAccessPointHeadersToBuilder(Message message, ValueBuilder<EmailValue> builder) {
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            Origin origin = (Origin) conversationOwner;
            EmailAccessPoint emailAccessPoint = origin.accesspoint().get();

            if (emailAccessPoint != null)
            {
               builder.prototype().from().set(emailAccessPoint.getDescription() );
               builder.prototype().headers().get().put( "Auto-Submitted", "auto-replied" );
               builder.prototype().headers().get().put( "X-Auto-Response-Suppress", "OOF, DR, RN, NRN" );
               builder.prototype().headers().get().put( "X-Autoreply", "yes" );
               builder.prototype().headers().get().put( "X-Autorespond", "yes" );
               builder.prototype().headers().get().put( "Precedence", "auto_reply" );
               builder.prototype().headers().get().put( "X-Precedence", "auto_reply" );
            }
         }

         private void addAttachmentsToBuilder(Message message,
               ValueBuilder<EmailValue> builder) {
            // add message attachments if any
            if ( message.hasAttachments()) {

               List<AttachedFileValue> attachments = builder.prototype().attachments().get();
               ValueBuilder<AttachedFileValue> attachment = module.valueBuilderFactory().newValueBuilder(AttachedFileValue.class);

               for (Attachment caseAttachment : ((Attachments.Data)message).attachments())
               {
                  AttachedFile.Data attachedFile = (AttachedFile.Data) caseAttachment;
                  attachment.prototype().mimeType().set(attachedFile.mimeType().get());
                  attachment.prototype().uri().set(attachedFile.uri().get());
                  attachment.prototype().modificationDate().set(attachedFile.modificationDate().get());
                  attachment.prototype().name().set(attachedFile.name().get());
                  attachment.prototype().size().set(attachedFile.size().get());
                  attachments.add( attachment.newInstance() );
               }
            }
         }

         private void addThreadingHeadersToBuilder(Conversation conversation,
               MessageReceiver recipient, ValueBuilder<EmailValue> builder)
               throws UnsupportedEncodingException {
            // Threading headers
            builder.prototype().messageId().set( "<"+conversation.toString()+"/"+ URLEncoder.encode(recipient.toString(), "UTF-8")+"@Streamflow>" );
            ManyAssociation<Message> messages = ((Messages.Data)conversation).messages();
            StringBuilder references = new StringBuilder();
            String inReplyTo = null;
            for (Message previousMessage : messages)
            {
               if (references.length() > 0)
                  references.append( " " );

               inReplyTo = "<"+previousMessage.toString()+"/"+URLEncoder.encode(recipient.toString(), "UTF-8")+"@Streamflow>";
               references.append( inReplyTo );
            }
            builder.prototype().headers().get().put( "References", references.toString() );
            if (inReplyTo != null)
               builder.prototype().headers().get().put( "In-Reply-To", inReplyTo );
         }

         private boolean shouldSendFullMail(Message message, MessageReceiver recipient) {
            MessageRecipient.Data recipientSettings = (MessageRecipient.Data) recipient;
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            boolean isRestricted = ((CaseAccessRestriction.Data) conversationOwner).restricted().get();

            return (
                  recipientSettings.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email )
                  && (!messageData.body().get().trim().isEmpty() || message.hasAttachments())
                  && determineRecipientEmailAddress(recipient) != null
                  && !isRestricted
                  );
         }

         private boolean shouldSendNotificationMail(Message message, MessageReceiver recipient) {
            MessageRecipient.Data recipientSettings = (MessageRecipient.Data) recipient;
            Message.Data messageData = (Message.Data) message;
            Conversation conversation = messageData.conversation().get();
            ConversationOwner conversationOwner = conversation.conversationOwner().get();
            boolean isRestricted = ((CaseAccessRestriction.Data) conversationOwner).restricted().get();
            boolean isRecipientRegularUser = recipient instanceof UserEntity;

            return (
                  recipientSettings.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email )
                  && (!messageData.body().get().trim().isEmpty() || message.hasAttachments())
                  && determineRecipientEmailAddress(recipient) != null
                  && (isRestricted && isRecipientRegularUser)
                  );
         }
      }
   }
}