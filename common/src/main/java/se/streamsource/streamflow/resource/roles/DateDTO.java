package se.streamsource.streamflow.resource.roles;

import java.util.Date;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

public interface DateDTO 
	extends ValueComposite 
{
	Property<Date> date();
}
