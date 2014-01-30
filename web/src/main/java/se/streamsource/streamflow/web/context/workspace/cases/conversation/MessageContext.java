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
package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

/**
 *
 */
@Concerns(UpdateCaseCountMessageContextConcern.class)
@Mixins(MessageContext.Mixin.class)
public interface MessageContext extends IndexContext<MessageDTO>, Context
{
   void read();
   
   abstract class Mixin implements MessageContext
   {
      @Structure
      Module module;

      public MessageDTO index()
      {
         ResourceBundle bundle = ResourceBundle
               .getBundle( MessagesContext.class.getName(), RoleMap.role( Locale.class ) );
         Map<String, String> translations = new HashMap<String, String>();
         for (String key : bundle.keySet())
         {
            translations.put( key, bundle.getString( key ) );
         }

         Message.Data messageData = RoleMap.current().get( Message.Data.class );
         Message message = RoleMap.role( Message.class );

         ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
         Contactable contact = module.unitOfWorkFactory().currentUnitOfWork()
               .get( Contactable.class, EntityReference.getEntityReference( messageData.sender().get() ).identity() );
         String sender = contact.getContact().name().get();
         builder
               .prototype()
               .sender()
               .set( !"".equals( sender ) ? sender : EntityReference.getEntityReference( messageData.sender().get() )
                     .identity() );
         builder.prototype().createdOn().set( messageData.createdOn().get() );
         builder.prototype().id().set( ((Identity) messageData).identity().get() );
         builder.prototype().href().set( builder.prototype().id().get() );
         builder.prototype().text().set( message.translateBody( translations ) );
         builder.prototype().hasAttachments().set( ((Message) messageData).hasAttachments() );
         builder.prototype().unread().set( ((Message) messageData).isUnread() );
         builder.prototype().messageType().set( messageData.messageType().get() );

         return builder.newInstance();
      }

      public void read()
      {
         RoleMap.role( Message.class ).setUnread( false );
      }
   }
}
