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
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(ConversationParticipants.Mixin.class)
public interface ConversationParticipants
{
   void addParticipant(ConversationParticipant participant);
   void removeParticipant(ConversationParticipant participant);

   boolean isParticipant( ConversationParticipant participant );

   void receiveMessage( Message message );

   interface Data
   {
      ManyAssociation<ConversationParticipant> participants();

      void addedParticipant( @Optional DomainEvent event, ConversationParticipant participant);
      void removedParticipant( @Optional DomainEvent event, ConversationParticipant participant);
   }

   abstract class Mixin
      implements ConversationParticipants, Data
   {
      public void addParticipant( ConversationParticipant participant )
      {
         if (!isParticipant( participant ))
            addedParticipant( null, participant );
      }

      public void removeParticipant( ConversationParticipant participant )
      {
         if (isParticipant( participant ))
            removedParticipant( null, participant );
      }

      public boolean isParticipant( ConversationParticipant participant )
      {
         return participants().contains( participant );
      }

      public void addedParticipant( DomainEvent event, ConversationParticipant participant )
      {
         participants().add( participant );
      }

      public void removedParticipant( DomainEvent event, ConversationParticipant participant )
      {
         participants().remove( participant );
      }

      public void receiveMessage( Message message )
      {
         for (ConversationParticipant conversationParticipant : participants())
         {
            conversationParticipant.receiveMessage( message );
         }
      }
   }
}
