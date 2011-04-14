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

package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.constraint.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * JAVADOC
 */
@Mixins(ConversationsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface ConversationsContext
      extends
      IndexContext<LinksValue>, Context
{
   public void create( @MaxLength(50) StringValue topic );

   abstract class Mixin
         implements ConversationsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<ConversationDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDTO.class );

         Conversations.Data conversations = RoleMap.role( Conversations.Data.class );

         for (Conversation conversation : conversations.conversations())
         {
            builder.prototype().creationDate().set( conversation.createdOn().get() );
            builder.prototype().creator().set( ((Describable) conversation.createdBy().get()).getDescription() );
            builder.prototype().messages().set( ((Messages.Data) conversation).messages().count() );
            builder.prototype().participants().set( ((ConversationParticipants.Data) conversation).participants().count() );
            builder.prototype().href().set( EntityReference.getEntityReference( conversation ).identity() );
            builder.prototype().text().set( conversation.getDescription() );
            builder.prototype().id().set( EntityReference.getEntityReference( conversation ).identity() );

            links.addLink( builder.newInstance() );
         }
         return links.newLinks();
      }

      public void create( StringValue topic )
      {
         Conversations conversations = RoleMap.role( Conversations.class );
         Conversation conversation = conversations.createConversation( topic.string().get(), RoleMap.role( Creator.class ) );
         ((ConversationEntity) conversation).addParticipant( RoleMap.role( ConversationParticipant.class ) );

      }
   }
}