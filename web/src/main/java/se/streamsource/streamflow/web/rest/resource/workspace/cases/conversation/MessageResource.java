package se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessageContext;
import se.streamsource.streamflow.web.rest.resource.workspace.cases.AttachmentsResource;

/**
 *
 */
public class MessageResource
      extends CommandQueryResource
{
   public MessageResource()
   {
      super( MessageContext.class );
   }

   @SubResource
   public void attachments()
   {
      subResource( AttachmentsResource.class );
   }

}
