package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseFormOnCloseContext;

/**
 * Resource for form on close case create and submit
 */
public class CaseFormOnCloseResource
   extends CommandQueryResource
{
   public CaseFormOnCloseResource()
   {
      super( CaseFormOnCloseContext.class );
   }

}
