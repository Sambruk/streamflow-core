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

package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.structure.created.*;

/**
 * JAVADOC
 */
@Mixins(Conversations.Mixin.class)
public interface Conversations
{
   Conversation createConversation(String topic, Creator creator);

   public boolean hasConversations();

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<Conversation> conversations();

      Conversation createdConversation( @Optional DomainEvent event, String id, Creator creator );
   }

   abstract class Mixin
      implements Conversations, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      ConversationOwner conversationOwner;

      @State
      ManyAssociation<Conversation> conversations;

      public Conversation createConversation( String topic, Creator creator )
      {
         Conversation conversation = createdConversation( null, idGen.generate( Identity.class ), creator);
         conversation.changeDescription( topic );

         return conversation;
      }

      public Conversation createdConversation( DomainEvent event, String id, Creator creator )
      {
         EntityBuilder<Conversation> builder = uowf.currentUnitOfWork().newEntityBuilder( Conversation.class, id );
         builder.instanceFor( CreatedOn.class).createdOn().set( event.on().get() );
         builder.instanceFor( CreatedOn.class).createdBy().set( creator );
         builder.instance().conversationOwner().set( conversationOwner );

         Conversation conversation = builder.newInstance();
         conversations().add( conversation );
         
         return conversation;
      }

      public boolean hasConversations()
      {
         return !conversations.toList().isEmpty();
      }
   }
}
