package se.streamsource.streamflow.web.resource.surface.administration.organizations.emailaccesspoints;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.AccessPointsAdministrationContext;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.EmailAccessPointAdministrationContext;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.EmailAccessPointsAdministrationContext;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints.AccessPointAdministrationResource;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class EmailAccessPointsAdministrationResource
        extends CommandQueryResource
        implements SubResources
{
   public EmailAccessPointsAdministrationResource()
   {
      super(EmailAccessPointsAdministrationContext.class);
   }

   public void resource(String segment) throws ResourceException
   {
      findList(role(EmailAccessPoints.Data.class).emailAccessPoints().get(), segment);
      subResourceContexts(EmailAccessPointAdministrationContext.class);
   }
}