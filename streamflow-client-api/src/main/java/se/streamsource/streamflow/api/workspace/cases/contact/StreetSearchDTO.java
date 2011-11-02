package se.streamsource.streamflow.api.workspace.cases.contact;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

public interface StreetSearchDTO
      extends ValueComposite
{
   @UseDefaults
   Property<String> address();

   @UseDefaults
   Property<String> area();
}