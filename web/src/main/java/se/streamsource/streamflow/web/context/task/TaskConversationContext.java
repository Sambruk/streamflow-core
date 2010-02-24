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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.DeleteContext;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.conversation.ConversationDetailDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
@Mixins(TaskConversationContext.Mixin.class)
public interface TaskConversationContext
   extends DeleteContext, Context, IndexContext<ConversationDetailDTO>
{
   public LinksValue messages();
   public LinksValue participants();
   public void addmessage(StringDTO message);

   abstract class Mixin
      extends ContextMixin
      implements TaskConversationContext
   {
      @Structure
      Module module;

      public ConversationDetailDTO index()
      {
         ValueBuilder<ConversationDetailDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDetailDTO.class );
         ConversationEntity conversation = context.role( ConversationEntity.class );

         builder.prototype().description().set( conversation.getDescription()  );
         builder.prototype().creationDate().set( conversation.createdOn().get() );
         builder.prototype().creator().set( conversation.createdBy().get().toString() );
         builder.prototype().messages().set( messages() );
         builder.prototype().participants().set( participants() );

         return builder.newInstance();
      }

      public LinksValue messages()
      {
         LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
         ConversationEntity conversation = context.role( ConversationEntity.class );

         for (Message message : conversation.messages())
         {
            builder.prototype().sender().set( EntityReference.getEntityReference( ((MessageEntity)message).sender().get()) );
            builder.prototype().createdOn().set( ((MessageEntity)message).createdOn().get() );
            builder.prototype().text().set( ((MessageEntity)message).body().get() );
            builder.prototype().href().set( ((MessageEntity)message).identity().get() );
            builder.prototype().id().set( ((MessageEntity)message).identity().get() );

            links.addLink( builder.newInstance() );
         }
         return links.newLinks();
      }

      public LinksValue participants()
      {
         return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( context.role(ConversationEntity.class).participants() ).newLinks();
      }

      public void delete()
      {
         /*Conversations conversations = context.role(Conversations.class);
         Integer index = context.role(Integer.class);

         conversations.deleteConversation( index );
         */
      }

      public void addmessage( StringDTO message )
      {
         Messages messages = context.role( Messages.class );
         messages.createMessage( message.string().get(), context.role( ConversationParticipant.class ));
      }
   }
}