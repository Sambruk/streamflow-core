package se.streamsource.streamflow.web.context.administration.surface;

import org.restlet.Response;
import se.streamsource.streamflow.web.context.workspace.cases.attachment.AttachmentsContext;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Attachments on the organization. These have differing permission requirements compared to the one on cases
 */
public class OrganizationAttachmentsContext
   extends AttachmentsContext
{
   @Override
   public void createattachment(Response response) throws IOException, URISyntaxException
   {
      super.createattachment(response);
   }
}
