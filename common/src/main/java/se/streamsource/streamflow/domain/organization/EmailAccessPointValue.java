package se.streamsource.streamflow.domain.organization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Value for connecting an incoming email address (in the "To:" field) to an AccessPoint.
 */
public interface EmailAccessPointValue
   extends ValueComposite
{
   Property<String> email();
   Property<EntityReference> accessPoint();
}
