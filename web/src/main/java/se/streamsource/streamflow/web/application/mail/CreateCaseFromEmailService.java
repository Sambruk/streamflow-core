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
package se.streamsource.streamflow.web.application.mail;

import info.ineighborhood.cardme.engine.VCardEngine;
import info.ineighborhood.cardme.vcard.VCard;
import info.ineighborhood.cardme.vcard.features.AddressFeature;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayer;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventReplayException;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationTransactionTracker;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Receive emails and create cases through Access Points
 */
@Mixins(CreateCaseFromEmailService.Mixin.class)
public interface CreateCaseFromEmailService
        extends Configuration, Activatable, ServiceComposite
{
   class Mixin
           implements Activatable
   {
      @Service
      ApplicationEventSource eventSource;

      @Service
      ApplicationEventStream stream;

      @Service
      AttachmentStore attachments;
      
      @Service
      SystemDefaultsService systemDefaults;

      @Structure
      Module module;

      @This
      Configuration<CreateCaseFromEmailConfiguration> config;

      private ApplicationTransactionTracker<ApplicationEventReplayException> tracker;

      @Service
      ApplicationEventPlayer player;

      private ReceiveEmails receiveEmails = new ReceiveEmails();
      private VCardEngine vcardEngine;

      public void activate() throws Exception
      {
         vcardEngine = new VCardEngine();

         Output<TransactionApplicationEvents, ApplicationEventReplayException> playerOutput = ApplicationEvents.playEvents(player, receiveEmails);

         tracker = new ApplicationTransactionTracker<ApplicationEventReplayException>(stream, eventSource, config, playerOutput);
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public class ReceiveEmails
              extends MailReceiver.Mixin
      {
         public void receivedEmail(ApplicationEvent event, EmailValue email)
         {
            int maxTries = 3;
            for (int i = 0; i < maxTries; i++)
            {
               UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Create case from email" ) );

               try
               {
                  String references = email.headers().get().get( "References" );

                  // This is not in response to something that we sent out - create new case from it

                  if (!hasStreamflowReference( references ))
                  {
                     Organizations.Data organizations = uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
                     Organization organization = organizations.organization().get();
                     EmailAccessPoint ap = null;
                     try
                     {
                        ap = organization.getEmailAccessPoint( email.to().get() );
                     } catch (IllegalArgumentException e)
                     {

                        // No AP for this email address - create support case.
                        ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class ).withPrototype( email );

                        String subj = "Unknown accesspoint: " + builder.prototype().to().get() + " - " + builder.prototype().subject().get();

                        builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                        systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                        uow.discard();
                        return;
                     }

                     Drafts user = systemDefaults.getUser( email );
                     ConversationParticipant participant = (ConversationParticipant) user;

                     RoleMap.newCurrentRoleMap();
                     RoleMap.current().set( organization );
                     RoleMap.current().set( ap );
                     RoleMap.current().set( user );

                     CaseEntity caze = ap.createCase( user );
                     RoleMap.current().set( caze );

                     caze.caselog().get().addTypedEntry( "{accesspoint,description=" + ap.getDescription() + "}", CaseLogEntryTypes.system );

                     // STREAMFLOW-714
                     String subject = email.subject().get();
                     caze.changeDescription( subject.length() > 50 ? subject.substring( 0, 50 ) : subject );

                     if (Translator.HTML.equalsIgnoreCase( email.contentType().get() ))
                     {
                        caze.addNote( email.contentHtml().get(), email.contentType().get().toLowerCase() );

                     } else
                     {
                        caze.addNote( email.content().get(), email.contentType().get().toLowerCase() );

                     }

                     // Create conversation
                     Conversation conversation = caze.createConversation( email.subject().get(), (Creator) user );
                     Message message = null;
                     if (Translator.HTML.equalsIgnoreCase( email.contentType().get() ))
                     {
                        message = conversation.createMessage( email.content().get(), MessageType.HTML, participant );
                     } else
                     {
                        message = conversation.createMessage( email.content().get(), MessageType.PLAIN, participant );
                     }
                     // Create attachments
                     for (AttachedFileValue attachedFileValue : email.attachments().get())
                     {
                        if (attachedFileValue.mimeType().get().contains( "text/x-vcard" )
                              || attachedFileValue.mimeType().get().contains( "text/directory" ))
                        {
                           addVCardAsContact( (Contactable.Data) user, attachedFileValue );
                        } else
                        {
                           Attachment attachment = conversation.createAttachment( attachedFileValue.uri().get() );
                           attachment.changeName( attachedFileValue.name().get() );
                           attachment.changeMimeType( attachedFileValue.mimeType().get() );
                           attachment.changeModificationDate( attachedFileValue.modificationDate().get() );
                           attachment.changeSize( attachedFileValue.size().get() );
                           attachment.changeUri( attachedFileValue.uri().get() );
                           message.addAttachment( attachment );
                        }
                     }

                     // Add contact info
                     caze.updateContact( 0, ((Contactable.Data) user).contact().get() );

                     // Open the case
                     ap.sendTo( caze );
                  }

                  uow.complete();
                  // success - return
                  return;
               } catch (Exception ex)
               {
                  if( i+1 < maxTries && ex instanceof ConcurrentEntityModificationException )
                  {
                     // discard uow and try again
                     uow.discard();
                     continue;
                  } else
                  {
                     ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class ).withPrototype( email );
                     String subj = "General error: " + builder.prototype().to().get() + " - " + builder.prototype().subject().get();
                     builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                     systemDefaults.createCaseOnEmailFailure( builder.newInstance() );

                     uow.discard();
                     throw new ApplicationEventReplayException( event, ex );
                  }
               } finally
               {
                  RoleMap.clearCurrentRoleMap();
               }
            }
         }

         private void addVCardAsContact(Contactable.Data user, AttachedFileValue attachedFileValue) throws IOException
         {
            // Add VCard info to contact and then remove it as attachment
            String[] mimeTypeParts = attachedFileValue.mimeType().get().split(";");
            String charSet = "UTF-8";
            for (String mimeTypePart : mimeTypeParts)
            {
               if (mimeTypePart.trim().startsWith("charset"))
               {
                  charSet = mimeTypePart.split("=")[1].trim();
               }
            }

            Input<ByteBuffer, IOException> input = attachments.attachment(attachedFileValue.uri().get().substring("store:".length()));
            input.transferTo(Outputs.systemOut());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            input.transferTo(Outputs.<Object>byteBuffer(baos));

            InputStream inputStream = null;
            try
            {
               inputStream = MimeUtility.decode(new ByteArrayInputStream(baos.toByteArray()), "quoted-printable");
            } catch (MessagingException e)
            {
               throw new IOException("Could not decode VCard", e);
            }

            String vcardString = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(charSet)));
            String line;
            while ((line = reader.readLine()) != null)
               vcardString += line +"\n";

            VCard vcard = vcardEngine.parse(vcardString);

            ContactBuilder contactBuilder = new ContactBuilder(user.contact().get(), module.valueBuilderFactory());

            boolean modified = false;

            // Check company
            if (vcard.getOrganizations().hasOrganizations())
            {
               contactBuilder.company(vcard.getOrganizations().getOrganizations().next());
               modified = true;
            }

            // Check phone numbers
            if (vcard.getTelephoneNumbers().hasNext())
            {
               contactBuilder.phoneNumber(vcard.getTelephoneNumbers().next().getTelephone());
               modified = true;
            }

            // Check address
            if (vcard.getAddresses().hasNext())
            {
               AddressFeature address = vcard.getAddresses().next();
               String addressString = address.getStreetAddress();
               if (address.getPostalCode() != null)
                  addressString+=", "+address.getPostalCode();
               if (address.getLocality() != null)
                  addressString+=", "+address.getLocality();
               if (address.getCountryName() != null)
                  addressString+= ", "+address.getCountryName();
               contactBuilder.address(addressString);
               modified = true;
            }

            // Check note
            if (vcard.getNotes().hasNext())
            {
               contactBuilder.note(vcard.getNotes().next().getNote());
               modified = true;
            }

            // Update contact info if necessary
            if (modified)
            {
               ((Contactable)user).updateContact(contactBuilder.newInstance());
            }

            attachments.deleteAttachment(attachedFileValue.uri().get().substring("store:".length()));
         }
      }
   }
}