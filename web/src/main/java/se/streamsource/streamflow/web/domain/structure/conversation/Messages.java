/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import static se.streamsource.streamflow.infrastructure.event.DomainEvent.CREATE;

/**
 * JAVADOC
 */
@Mixins(Messages.Mixin.class)
public interface Messages
{
   Message createMessage(String body, ConversationParticipant participant) throws IllegalArgumentException;

   interface Data
   {
      ManyAssociation<Message> messages();

      Message createdMessage( DomainEvent create, String id, String body, ConversationParticipant participant );
   }

   abstract class Mixin
      implements Messages, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      ConversationParticipants participants;

      @This
      Conversation conversation;

      public Message createMessage( String body, ConversationParticipant participant ) throws IllegalArgumentException
      {
         if (participants.isParticipant(participant))
         {
            Message message = createdMessage( CREATE, idGen.generate( Identity.class ), body, participant);

            participants.receiveMessage(message);

            return message;
         } else
         {
            throw new IllegalArgumentException("Participant is not a member of this conversation");
         }
      }

      public Message createdMessage( DomainEvent event, String id, String body, ConversationParticipant participant )
      {
         EntityBuilder<Message> builder = uowf.currentUnitOfWork().newEntityBuilder( Message.class, id );
         builder.instanceFor( Message.Data.class ).body().set( body );
         builder.instanceFor( Message.Data.class ).createdOn().set( event.on().get() );
         builder.instanceFor( Message.Data.class ).sender().set( participant );
         builder.instanceFor( Message.Data.class ).conversation().set( conversation );

         Message message = builder.newInstance();
         messages().add( message );

         return message;
      }
   }
}