package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Representation of the OU structure. This uses the Nested Dataset concept, so that
 * it is possible to do nested SQL queries for the case data.
 */
public interface OrganizationalUnitValue
   extends ValueComposite
{
   Property<String> name();
   Property<String> id();
   Property<Integer> left();
   Property<Integer> right();
}
