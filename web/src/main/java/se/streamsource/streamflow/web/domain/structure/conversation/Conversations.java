/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * JAVADOC
 */
@Mixins(Conversations.Mixin.class)
public interface Conversations
{
   Conversation createConversation(String topic, Creator creator);

   public boolean hasConversations();

   void removeConversation( Conversation conversation );

   boolean hasUnreadConversation();

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<Conversation> conversations();

      Conversation createdConversation( @Optional DomainEvent event, String id, Creator creator );

      public void removedConversation( @Optional DomainEvent event, Conversation conversation );
   }

   abstract class Mixin
      implements Conversations, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

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
         EntityBuilder<Conversation> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Conversation.class, id );
         builder.instanceFor( CreatedOn.class).createdOn().set( event.on().get() );
         builder.instanceFor( CreatedOn.class).createdBy().set( creator );
         builder.instance().conversationOwner().set( conversationOwner );

         Conversation conversation = builder.newInstance();
         conversations().add( conversation );
         
         return conversation;
      }

      public void removeConversation( Conversation conversation )
      {
         if( conversations().contains( conversation ) )
         {
            removedConversation( null, conversation );
         }
      }

      public void removedConversation( @Optional DomainEvent event, Conversation conversation )
      {
         conversations().remove( conversation );
         conversation.deleteEntity();
      }

      public boolean hasConversations()
      {
         return !conversations.toList().isEmpty();
      }

      public boolean hasUnreadConversation()
      {
         return Iterables.matchesAny( new Specification<Conversation>()
         {
            public boolean satisfiedBy( final Conversation conversation )
            {
               return conversation.hasUnreadMessage();
            }
         }, conversations
         );
      }
   }
}
