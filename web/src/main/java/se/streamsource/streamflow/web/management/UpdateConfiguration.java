package se.streamsource.streamflow.web.management;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface UpdateConfiguration
   extends ConfigurationComposite
{
   @UseDefaults
   Property<String> lastStartupVersion();
}
