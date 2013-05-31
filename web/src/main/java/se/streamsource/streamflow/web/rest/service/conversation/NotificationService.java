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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
            HtmlMailGenerator htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
            try
            {
               Message.Data messageData = (Message.Data) message;

               Conversation conversation = messageData.conversation().get();

               ConversationOwner owner = conversation.conversationOwner().get();

               // check if sender is administrator and in that case dont set fromName - this will be picked up by
               // MailSender and replaced with the configurated default fromName
               String sender = null;
               String footer ="";
               if( ! EntityReference.getEntityReference( messageData.sender().get() ).identity().equals( UserEntity.ADMINISTRATOR_USERNAME ))
               {
                  sender = ((Contactable.Data) messageData.sender().get()).contact().get().name().get();
                  if( messageData.sender().get() instanceof MailFooter )
                  {
                     footer = ((MailFooter.Data)messageData.sender().get()).footer().get();
                  }
               }

               String caseId = "n/a";

               if (owner != null)
                  caseId = ((CaseId.Data) owner).caseId().get() != null ? ((CaseId.Data) owner).caseId().get() : "n/a";

               MessageReceiver user = uow.get( MessageReceiver.class, event.entity().get() );

               MessageRecipient.Data recipientSettings = (MessageRecipient.Data) user;

               if (recipientSettings.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email ))
               {
                  String subject;
                  String formattedMsg;

                  Origin origin = (Origin) owner;
                  EmailAccessPoint emailAccessPoint = origin.accesspoint().get();

                  if (emailAccessPoint != null)
                  {
                     formattedMsg = message.translateBody(emailAccessPoint.emailTemplates().get());
                     subject = MessageTemplate.text(emailAccessPoint.subject().get()).bind("caseid", caseId).bind("subject", conversation.getDescription()).eval();
                  } else
                  {
                     formattedMsg = message.translateBody(templateDefaults);
                     subject = "[" + caseId + "] " + conversation.getDescription(); // Default subject format
                  }

                  if (formattedMsg.trim().equals(""))
                     return; // Don't try to send empty messages

                  ContactEmailDTO recipientEmail = ((Contactable.Data)user).contact().get().defaultEmail();
                  if (recipientEmail != null)
                  {
                     ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);
                     builder.prototype().fromName().set( sender );

                     if (emailAccessPoint != null)
                     {
                        builder.prototype().from().set(emailAccessPoint.getDescription() );
                        builder.prototype().headers().get().put( "Auto-Submitted", "auto-replied" );
                     }

      //               builder.prototype().replyTo();
                     builder.prototype().to().set( recipientEmail.emailAddress().get() );
                     builder.prototype().subject().set( subject );
                     builder.prototype().content().set( htmlGenerator.createMailContent( formattedMsg, footer ) );
                     builder.prototype().contentType().set( Translator.HTML );

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

                     // Threading headers
                     builder.prototype().messageId().set( "<"+conversation.toString()+"/"+ URLEncoder.encode(user.toString(), "UTF-8")+"@Streamflow>" );
                     ManyAssociation<Message> messages = ((Messages.Data)conversation).messages();
                     StringBuilder references = new StringBuilder();
                     String inReplyTo = null;
                     for (Message previousMessage : messages)
                     {
                        if (references.length() > 0)
                           references.append( " " );

                        inReplyTo = "<"+previousMessage.toString()+"/"+URLEncoder.encode(user.toString(), "UTF-8")+"@Streamflow>";
                        references.append( inReplyTo );
                     }
                     builder.prototype().headers().get().put( "References", references.toString() );
                     if (inReplyTo != null)
                        builder.prototype().headers().get().put( "In-Reply-To", inReplyTo );

                     EmailValue emailValue = builder.newInstance();

                     mailSender.sentEmail( null, emailValue );
                  }
               }
            } catch (Throwable e)
            {
               logger.error("Could not send notification to user entity = " +  event.entity().get() , e);
            }
         }
      }
   }
}