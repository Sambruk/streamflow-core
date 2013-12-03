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
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

public abstract class UpdateCaseCountMessagesConcern extends ConcernOf<Messages> implements Messages
{

   Caching caching;
   
   public void init(@Optional @Service CachingService cache)
   {
      caching = new Caching( cache, Caches.CASECOUNTS );
   }

   public Message createMessage( String body, MessageType messageType, ConversationParticipant participant ) throws IllegalArgumentException
   {
      Case caze = RoleMap.role( Case.class );
      boolean isUnreadFromStart = caze.isUnread();
      
      Message createdMessage = next.createMessage( body, messageType, participant );

      
      if (caze.isUnread() && !isUnreadFromStart && ((Ownable.Data)caze).owner().get() != null )
      {
         if (caze.isAssigned())
         {
            // Update assignments for user
            Assignee assignee = ((Assignable.Data) caze).assignedTo().get();
            caching.addToUnreadCache( ((Ownable.Data)caze).owner().get().toString() + ":" + assignee.toString(), 1 );
         } else
         {
            // Update inbox cache
            caching.addToUnreadCache( ((Ownable.Data)caze).owner().get().toString(), 1 );
         }
      }
      return createdMessage;
   }
}