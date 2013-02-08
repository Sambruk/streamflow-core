package se.streamsource.streamflow.web.rest.resource.external;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.external.ShadowCaseContext;


public class ShadowCaseResource
   extends CommandQueryResource
{
   public ShadowCaseResource()
   {
      super( ShadowCaseContext.class );
   }
}
