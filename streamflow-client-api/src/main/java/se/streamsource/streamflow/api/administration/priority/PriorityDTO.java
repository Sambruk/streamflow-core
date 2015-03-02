package se.streamsource.streamflow.api.administration.priority;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

public interface PriorityDTO 
    extends ValueComposite 
{
    Property<Integer> priority();
    Property<String> text();
    Property<String> id();
}
