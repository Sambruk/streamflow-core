package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;


public interface DefaultSystemConfiguration
      extends ConfigurationComposite, Enabled
{
   Property<Boolean> ascending();
}
