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
package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;

/**
 * JAVADOC
 */
@Mixins(Messages.Mixin.class)
public interface Messages
{
   Message createMessage(String body, ConversationParticipant participant);

   Message getLastMessage();

   void createMessageFromDraft( ConversationParticipant participant );

   interface Data
   {
      ManyAssociation<Message> messages();

      Message createdMessage( @Optional DomainEvent create, String id, String body, ConversationParticipant participant );

      Message createdMessageFromDraft( @Optional DomainEvent event, String id, MessageDraft draft, ConversationParticipant participant );
   }

   abstract class Mixin
      implements Messages, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      ConversationParticipants participants;

      @This
      Conversation conversation;

      public Message createMessage( String body, ConversationParticipant participant ) throws IllegalArgumentException
      {
         if (!participants.isParticipant(participant))
         {
            participants.addParticipant( participant );
         }

         Message message = createdMessage( null, idGen.generate( Identity.class ), body, participant );

         participants.receiveMessage(message);

         return message;
      }

      public Message createdMessage( DomainEvent event, String id, String body, ConversationParticipant participant )
      {
         EntityBuilder<Message> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Message.class, id );
         builder.instanceFor( Message.Data.class ).body().set( body );
         builder.instanceFor( Message.Data.class ).createdOn().set( event.on().get() );
         builder.instanceFor( Message.Data.class ).sender().set( participant );
         builder.instanceFor( Message.Data.class ).conversation().set( conversation );

         Message message = builder.newInstance();
         messages().add( message );

         return message;
      }

      public Message getLastMessage()
      {
         if (messages().count() > 0)
            return messages().get(messages().count()-1);
         else
            return null;
      }

      public void createMessageFromDraft( ConversationParticipant participant )
      {
         if (!participants.isParticipant(participant))
         {
            participants.addParticipant( participant );
         }

         Message message = createdMessageFromDraft( null, idGen.generate( Identity.class ), ((MessageDraft)conversation), participant );
         for( Attachment attachment : ((Attachments.Data)conversation).attachments().toList() )
         {
            message.addAttachment( attachment );
            // remove attachment from draft attachments data so AttachmentEntity does not get
            // removed for real - we just moved it to message attachments where it actually belongs after
            // message creation.
            ((Attachments.Data)conversation).attachments().remove( attachment );
         }

         // also reset draft message body
         ((MessageDraft)conversation).changeDraftMessage( null );

         participants.receiveMessage(message);

      }

      public Message createdMessageFromDraft( @Optional DomainEvent event, String id, MessageDraft draft, ConversationParticipant participant )
      {
         EntityBuilder<Message> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Message.class, id );
         builder.instanceFor( Message.Data.class ).body().set( ((MessageDraft.Data)draft).draftmessage().get() );
         builder.instanceFor( Message.Data.class ).createdOn().set( event.on().get() );
         builder.instanceFor( Message.Data.class ).sender().set( participant );
         builder.instanceFor( Message.Data.class ).conversation().set( conversation );

         Message message = builder.newInstance();
         messages().add( message );

         return message;
      }
   }
}