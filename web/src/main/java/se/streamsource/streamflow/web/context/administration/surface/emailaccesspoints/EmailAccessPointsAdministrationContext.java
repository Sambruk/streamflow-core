package se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;

import java.util.List;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class EmailAccessPointsAdministrationContext
        implements CreateContext<EmailAccessPointValue>, IndexContext<TableValue>
{
   @Structure
   Module module;

   public TableValue index()
   {
      EmailAccessPoints.Data data = role(EmailAccessPoints.Data.class);

      TableBuilder tableBuilder = new TableBuilder(module.valueBuilderFactory());
      tableBuilder.column("email", "Email", "string").column("accesspoint", "Access point", "string");

      List<EmailAccessPointValue> emailAccessPoints = data.emailAccessPoints().get();
      for (int i = 0; i < emailAccessPoints.size(); i++)
      {
         EmailAccessPointValue emailAccessPointValue = emailAccessPoints.get(i);
         tableBuilder.row().
                 cell(emailAccessPointValue.email().get(), emailAccessPointValue.email().get()).
                 cell(emailAccessPointValue.accessPoint().get().identity(), module.unitOfWorkFactory().currentUnitOfWork().get(Describable.class, emailAccessPointValue.accessPoint().get().identity()).getDescription()).
         endRow();
      }

      return tableBuilder.newTable();
   }

   public LinksValue possibleAccessPoints()
   {
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());
      AccessPoints.Data accessPoints = role(AccessPoints.Data.class);
      for (AccessPoint accessPoint : accessPoints.accessPoints())
      {
         linksBuilder.addLink(accessPoint.getDescription(), accessPoint.toString(), "create", "create?accessPoint="+accessPoint.toString(), "");
      }

      return linksBuilder.newLinks();
   }

   public void create(EmailAccessPointValue value)
   {
      EmailAccessPoints eap = role(EmailAccessPoints.class);
      eap.addEmailAccessPoint(value);
   }
}
