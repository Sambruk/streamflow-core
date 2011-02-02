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

package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JAVADOC
 */
public class MessagesContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;
   
   public LinksValue index()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
      ConversationEntity conversation = RoleMap.role( ConversationEntity.class );

      for (Message message : conversation.messages())
      {
         Contactable contact = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
         String sender = contact.getContact().name().get();
         builder.prototype().sender().set( !"".equals( sender )
               ? sender
               : EntityReference.getEntityReference( ((MessageEntity) message).sender().get() ).identity() );
         builder.prototype().createdOn().set( ((MessageEntity) message).createdOn().get() );
         builder.prototype().text().set( translate( ( (MessageEntity) message).body().get(),RoleMap.role( Locale.class ) ) );
         builder.prototype().href().set( ((MessageEntity) message).identity().get() );
         builder.prototype().id().set( ((MessageEntity) message).identity().get() );

         links.addLink( builder.newInstance() );
      }
      return links.newLinks();
   }

   public void createmessage( StringValue message ) throws ResourceException
   {
      try
      {
         Messages messages = RoleMap.role( Messages.class );
         messages.createMessage( message.string().get(), RoleMap.role( ConversationParticipant.class ) );
      } catch (IllegalArgumentException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, e.getMessage() );
      }
   }

   private String translate( String message, Locale locale )
   {
      ResourceBundle bundle = ResourceBundle.getBundle( MessagesContext.class.getName(), locale );
      if( message.startsWith("{") && message.endsWith("}") )
      {
         String[] tokens = message.substring(1,message.length()-1).split( "," );
         String form = bundle.getString( tokens[0] );
         message = new MessageFormat( form, locale ).format( form, tokens.length > 1 ? tokens[1] : "" );
      }
      return message;
   }
}