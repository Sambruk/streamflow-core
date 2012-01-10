package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseLogContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseLogEntryContext;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;


public class CaseLogResource
   extends CommandQueryResource implements SubResources
{

   public CaseLogResource()
   {
      super( CaseLogContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      findList( ((CaseLog.Data) RoleMap.role( CaseLoggable.Data.class ).caselog().get()).entries().get(), segment );
      subResourceContexts( CaseLogEntryContext.class );
   }
}
