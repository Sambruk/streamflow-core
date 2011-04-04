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

package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;

/**
 * JAVADOC
 */
@Mixins(History.Mixin.class)
public interface History
{
   void addHistoryComment(String comment, ConversationParticipant participant);

   Conversation getHistory();

   AccessPoint getOriginalAccessPoint();

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

      public AccessPoint getOriginalAccessPoint()
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
               AccessPointEntity ap = qbf.newQueryBuilder(AccessPointEntity.class).where(QueryExpressions.eq(QueryExpressions.templateFor(Describable.Data.class).description(), accessPointName)).newQuery(uowf.currentUnitOfWork()).find();
               return ap;
            }
         }

         return null; // No AccessPoint was used to receive this case
      }
   }
}
