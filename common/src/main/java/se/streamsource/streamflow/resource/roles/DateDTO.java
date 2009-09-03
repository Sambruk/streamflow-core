package se.streamsource.streamflow.resource.roles;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

public interface DateDTO
        extends ValueComposite
{
    Property<Date> date();
}
