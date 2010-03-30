/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
@Mixins(ConversationContext.Mixin.class)
public interface ConversationContext
      extends /*DeleteContext,*/ Context, IndexContext<ConversationDTO>
{
   @SubContext
   MessagesContext messages();

   @SubContext
   ConversationParticipantsContext participants();

   abstract class Mixin
         extends ContextMixin
         implements ConversationContext
   {
      @Structure
      Module module;

      public ConversationDTO index()
      {
         ValueBuilder<ConversationDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDTO.class );
         ConversationEntity conversation = context.role( ConversationEntity.class );

         builder.prototype().id().set( conversation.identity().get() );
         builder.prototype().href().set( conversation.identity().get() );                           
         builder.prototype().text().set( conversation.getDescription() );
         builder.prototype().creationDate().set( conversation.createdOn().get() );
         builder.prototype().creator().set( ((Describable)conversation.createdBy().get() ).getDescription());
         builder.prototype().messages().set( ((Messages.Data)conversation).messages().count() );
         builder.prototype().participants().set( ((ConversationParticipants.Data)conversation).participants().count() );

         return builder.newInstance();
      }

      /*public void delete()
      {
         Conversations conversations = context.role(Conversations.class);
         Integer index = context.role(Integer.class);

         conversations.deleteConversation( index );

      } */

      public ConversationParticipantsContext participants()
      {
         return subContext( ConversationParticipantsContext.class );
      }

      public MessagesContext messages()
      {
         return subContext( MessagesContext.class );
      }
   }
}