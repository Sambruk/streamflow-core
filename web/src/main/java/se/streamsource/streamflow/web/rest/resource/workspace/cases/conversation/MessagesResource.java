package se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation;

import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 *
 *
 */
public class MessagesResource
      extends CommandQueryResource
      implements SubResources
{
   public MessagesResource()
   {
      super( MessagesContext.class );
   }

   public void resource( String segment ) throws ContextNotFoundException
   {
      findManyAssociation( RoleMap.role( Messages.Data.class ).messages(), segment);
      subResource( MessageResource.class);
   }
}