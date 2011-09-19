package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 * TODO
 */
public interface OrganizationalStructureValue
   extends ValueComposite
{
   @UseDefaults
   Property<List<OrganizationalUnitValue>> structure();
}
