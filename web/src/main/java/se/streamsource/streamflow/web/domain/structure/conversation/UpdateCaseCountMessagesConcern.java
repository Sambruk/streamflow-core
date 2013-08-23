package se.streamsource.streamflow.web.domain.structure.conversation;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

public abstract class UpdateCaseCountMessagesConcern extends ConcernOf<Messages>
{

   Caching caching;

   public void init(@Optional @Service CachingService cache)
   {
      caching = new Caching( cache, Caches.CASECOUNTS );
   }

   Message createMessage(String body, MessageType messageType, ConversationParticipant participant, boolean unread)
   {
      Message createdMessage = next.createMessage( body, messageType, participant, unread );

      CaseEntity caze = RoleMap.role( CaseEntity.class );

      if (unread)
      {
         if (caze.isAssigned())
         {
            // Update assignments for user
            Assignee assignee = role( Assignee.class );
            caching.addToUnreadCache( caze.owner().get().toString() + ":" + assignee.toString(), 1 );
         } else
         {
            // Update inbox cache
            caching.addToUnreadCache( caze.owner().get().toString(), 1 );
         }
      }
      return createdMessage;
   }
}