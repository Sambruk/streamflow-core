package se.streamsource.streamflow.api.surface;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;


public interface AccessPointSettingsDTO extends ValueComposite
{

   @Optional
   Property<String> cssfile();
   
   @Optional
   Property<String> location();
   
   @Optional
   Property<Integer> zoomLevel();
}
