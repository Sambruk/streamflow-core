package se.streamsource.streamflow.api.administration.priority;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import se.streamsource.dci.value.link.LinkValue;

/**
 *
 * Data transfer object extending LinkValue for case priority data.
 */
public interface CasePriorityDTO
   extends LinkValue
{
   @Optional
   Property<CasePriorityValue> priority();
}
