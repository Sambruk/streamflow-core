package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Immutable;


public interface ConversationOwnable
{
   @Immutable
   Association<ConversationOwner> conversationOwner();
}
