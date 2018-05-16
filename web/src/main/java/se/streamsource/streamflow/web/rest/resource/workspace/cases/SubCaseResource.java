package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.SubCaseContext;

/**
 * Created by dmizem from Ubrainians for imCode on 16.05.18.
 */
public class SubCaseResource extends CommandQueryResource implements SubResources {
    public SubCaseResource() {
        super(SubCaseContext.class);
        System.out.println("asked for resource subcase");

    }

    public void resource(String segment) throws ResourceException {
        System.out.println("asked for resource subcase");
//      findList(RoleMap.role( Contacts.Data.class ).contacts().get(), segment);
//      subResourceContexts( ContactContext.class );
//
//      findList( ((CaseLog.Data) RoleMap.role( CaseLoggable.Data.class ).caselog().get()).entries().get(), segment );
//      subResourceContexts( CaseLogEntryContext.class );
    }
}

