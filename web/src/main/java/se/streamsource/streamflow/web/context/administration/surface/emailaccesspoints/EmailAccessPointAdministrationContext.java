package se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;

import java.io.IOException;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class EmailAccessPointAdministrationContext
        implements UpdateContext<EmailAccessPointValue>, IndexContext<EmailAccessPointValue>, DeleteContext
{
   @Structure
   Module module;

   public void delete() throws ResourceException, IOException
   {
      role(EmailAccessPoints.class).removeEmailAccessPoint(role(EmailAccessPointValue.class).email().get());
   }

   public void update(EmailAccessPointValue value)
   {
      role(EmailAccessPoints.class).addEmailAccessPoint(value);
   }

   public EmailAccessPointValue index()
   {
      return role(EmailAccessPointValue.class);
   }

   public void create(EmailAccessPointValue value)
   {
      EmailAccessPoints eap = role(EmailAccessPoints.class);
      eap.addEmailAccessPoint(value);
   }
}
