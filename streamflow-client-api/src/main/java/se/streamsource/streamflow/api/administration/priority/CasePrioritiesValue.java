package se.streamsource.streamflow.api.administration.priority;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 *
 * Class containing a list of case priority values.
 */
public interface CasePrioritiesValue
   extends ValueComposite
{
   @UseDefaults
   Property<List<CasePriorityValue>> priorities();
}
