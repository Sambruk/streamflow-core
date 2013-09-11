package se.streamsource.streamflow.api.administration.form;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import se.streamsource.streamflow.api.workspace.cases.location.CaseAddressDTO;

public interface LocationDTO extends ValueComposite
{
   @UseDefaults
   Property<String> location();
   
   Property<CaseAddressDTO> address();
}
