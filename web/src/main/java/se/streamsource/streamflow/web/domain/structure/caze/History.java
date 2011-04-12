/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.dci.api.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.entity.conversation.*;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.structure.conversation.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;

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

   EmailAccessPoint getOriginalEmailAccessPoint();

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
      UnitOfWorkFactory uowf;

      @This
      Case caze;

      @Structure
      QueryBuilderFactory qbf;

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
         history.createMessage( comment, participant );
      }

      public Conversation createdHistory( @Optional DomainEvent event, String id )
      {
         EntityBuilder<ConversationEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( ConversationEntity.class, id );
         builder.instance().conversationOwner().set( caze );
         builder.instance().createdBy().set( caze.createdBy().get() );
         builder.instance().createdOn().set( caze.createdOn().get() );
         ConversationEntity history = builder.newInstance();
         history.changeDescription( "History" );
         history.createMessage( "{created," + ((Describable)caze.createdBy().get()).getDescription() +"}", RoleMap.role( ConversationParticipant.class ));
         history().set(history);

         return history;
      }

      public EmailAccessPoint getOriginalEmailAccessPoint()
      {
         Messages.Data messages = ((Messages.Data) data.history().get());
         for (Message message : messages.messages())
         {
            String body = ((Message.Data) message).body().get();
            if (body.startsWith("{accesspoint"))
            {
               // This is the history message that the case has been received through a particular AccessPoint
               String accessPointName = body.substring(body.indexOf(",")+1, body.length()-1);

               // Now find it
               EmailAccessPointEntity ap = qbf.newQueryBuilder(EmailAccessPointEntity.class).where(QueryExpressions.eq(QueryExpressions.templateFor(Describable.Data.class).description(), accessPointName)).newQuery(uowf.currentUnitOfWork()).find();
               return ap;
            }
         }

         return null; // No AccessPoint was used to receive this case
      }
   }
}
