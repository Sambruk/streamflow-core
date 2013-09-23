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
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.interaction.gtd.Unread;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountMessagesConcern.class)
@Mixins(Messages.Mixin.class)
public interface Messages
{
   Message createMessage(String body, MessageType messageType, ConversationParticipant participant);

   Message createMessage(String body, MessageType messageType, ConversationParticipant participant, boolean unread );

   Message getLastMessage();

   void createMessageFromDraft( ConversationParticipant participant );

   void createMessageFromDraft( ConversationParticipant participant, MessageType messageType );

   boolean hasUnreadMessage();

   void markRead();

   interface Data
   {
      ManyAssociation<Message> messages();

      Message createdMessage( @Optional DomainEvent event, String id, String body, MessageType messageType, ConversationParticipant participant, boolean unread );

      Message createdMessageFromDraft( @Optional DomainEvent event, String id, MessageDraft draft, ConversationParticipant participant, MessageType messageType );
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

      public Message createMessage( String body, MessageType messageType, ConversationParticipant participant ) throws IllegalArgumentException
      {
         return createMessage( body, messageType, participant, true );
      }

      public Message createMessage( String body, MessageType messageType, ConversationParticipant participant, boolean unread ) throws IllegalArgumentException
      {
         if (!participants.isParticipant(participant))
         {
            participants.addParticipant( participant );
         }

         Message message = createdMessage( null, idGen.generate( Identity.class ), body, messageType, participant, unread );

         participants.receiveMessage(message);

         return message;
      }

      public Message createdMessage( DomainEvent event, String id, String body, MessageType messageType, ConversationParticipant participant, boolean unread )
      {
         EntityBuilder<Message> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Message.class, id );
         builder.instanceFor( Message.Data.class ).body().set( body );
         builder.instanceFor( Message.Data.class ).createdOn().set( event.on().get() );
         builder.instanceFor( Message.Data.class ).sender().set( participant );
         builder.instanceFor( Message.Data.class ).conversation().set( conversation );
         builder.instanceFor( Message.Data.class ).messageType().set( messageType );

         if( unread )
         {
            builder.instanceFor( Unread.Data.class ).unread().set( true );
         }

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
         createMessageFromDraft( participant, MessageType.PLAIN );
      }

      public void createMessageFromDraft( ConversationParticipant participant, MessageType messageType )
      {
         if (!participants.isParticipant(participant))
         {
            participants.addParticipant( participant );
         }

         Message message = createdMessageFromDraft( null, idGen.generate( Identity.class ), ((MessageDraft)conversation), participant, messageType );
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

      public Message createdMessageFromDraft( @Optional DomainEvent event, String id, MessageDraft draft, ConversationParticipant participant, MessageType messageType )
      {
         EntityBuilder<Message> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Message.class, id );
         builder.instanceFor( Message.Data.class ).body().set( ((MessageDraft.Data)draft).draftmessage().get() );
         builder.instanceFor( Message.Data.class ).createdOn().set( event.on().get() );
         builder.instanceFor( Message.Data.class ).sender().set( participant );
         builder.instanceFor( Message.Data.class ).conversation().set( conversation );
         builder.instanceFor( Message.Data.class ).messageType().set( messageType );

         Message message = builder.newInstance();
         messages().add( message );

         return message;
      }

      public boolean hasUnreadMessage()
      {
         return Iterables.matchesAny( new Specification<Message>()
         {
            public boolean satisfiedBy( Message msg )
            {
               return ((Unread.Data) msg).unread().get();
            }
         }, messages() );
      }

      public void markRead()
      {
         for( Message message : messages() )
         {
            message.setUnread( false );
         }
      }
   }
}