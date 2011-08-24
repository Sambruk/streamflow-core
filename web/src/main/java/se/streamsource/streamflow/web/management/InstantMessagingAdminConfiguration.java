package se.streamsource.streamflow.web.management;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface InstantMessagingAdminConfiguration
   extends ConfigurationComposite, Enabled
{
   Property<String> server();
   Property<String> user();
   Property<String> password();
}
