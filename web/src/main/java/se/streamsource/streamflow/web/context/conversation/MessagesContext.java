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

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
@Mixins(MessagesContext.Mixin.class)
public interface MessagesContext
      extends SubContexts<MessageContext>, IndexInteraction<LinksValue>, Interactions
{
   public void addmessage( StringValue message ) throws ResourceException;

   abstract class Mixin
         extends InteractionsMixin
         implements MessagesContext
   {
      public LinksValue index()
      {
         LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
         ConversationEntity conversation = context.get( ConversationEntity.class );

         for (Message message : conversation.messages())
         {
            Contactable contact = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
            String sender = contact.getContact().name().get();
            builder.prototype().sender().set( !"".equals( sender )
                  ? sender
                  : EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
            builder.prototype().createdOn().set( ((MessageEntity) message).createdOn().get() );
            builder.prototype().text().set( ((MessageEntity) message).body().get() );
            builder.prototype().href().set( ((MessageEntity) message).identity().get() );
            builder.prototype().id().set( ((MessageEntity) message).identity().get() );

            links.addLink( builder.newInstance() );
         }
         return links.newLinks();
      }

      public void addmessage( StringValue message ) throws ResourceException
      {
         try
         {
            Messages messages = context.get( Messages.class );
            messages.createMessage( message.string().get(), context.get( ConversationParticipant.class ) );
         } catch (IllegalArgumentException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, e.getMessage() );
         }
      }


      public MessageContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get( Message.class, id ) );
         return subContext( MessageContext.class );
      }
   }
}