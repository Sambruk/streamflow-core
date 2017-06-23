package se.streamsource.streamflow.api.workspace.cases.general;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 * Union of several {@link FieldValueDTO}
 */
public interface FieldValuesDTO
        extends ValueComposite
{

    Property<List<FieldValueDTO>> fieldValues();

}
