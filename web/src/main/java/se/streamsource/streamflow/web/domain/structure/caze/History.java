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
package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
@Mixins(History.Mixin.class)
public interface History
{
   void addHistoryComment(String comment, ConversationParticipant participant);

   Conversation getHistory();

   /**
    * Get last message that corresponds to the given standard type.
    *
    * Example: The case has been closed, and so a message has been recorded for this:
    * {closed}
    *
    * Calling getHistoryMessage("closed") will return this message.
    *
    * @param type of message
    * @return the last message matching this type or null if none match
    */
   Message.Data getHistoryMessage(String type);

   interface Data
   {
      @Optional
      Association<Conversation> history();

      Conversation createdHistory(@Optional DomainEvent event, String id);
   }

   abstract class Mixin
      implements History, Data
   {
      @This Data data;

      @Service
      IdentityGenerator idgen;

      @Structure
      Module module;

      @This
      Case caze;

      public Conversation getHistory()
      {
         Conversation history = data.history().get();
         if (history == null)
         {
            history = data.createdHistory( null, idgen.generate( ConversationEntity.class ) );
         }
         return history;
      }

      public Message.Data getHistoryMessage(String type)
      {
         Conversation conversation = getHistory();
         Message.Data foundMessage = null;
         for (Message message : ((Messages.Data) conversation).messages())
         {
            if (((Message.Data)message).body().get().startsWith("{"+type))
               foundMessage = (Message.Data) message;
         }
         return foundMessage; // No messages match this
      }

      public void addHistoryComment(String comment, ConversationParticipant participant)
      {
         Conversation history = getHistory();

         if (!history.isParticipant( participant ))
            history.addParticipant( participant );
         history.createMessage( comment, MessageType.SYSTEM, participant );
      }

      public Conversation createdHistory( @Optional DomainEvent event, String id )
      {
         EntityBuilder<ConversationEntity> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( ConversationEntity.class, id );
         builder.instance().conversationOwner().set( caze );
         builder.instance().createdBy().set( caze.createdBy().get() );
         builder.instance().createdOn().set( caze.createdOn().get() );
         ConversationEntity history = builder.newInstance();
         history.changeDescription( "History" );
         history.createMessage( "{created,creator=" + ((Describable)caze.createdBy().get()).getDescription() +"}", MessageType.SYSTEM, RoleMap.role( ConversationParticipant.class ));
         history().set(history);

         return history;
      }

   }
}
