package se.streamsource.streamflow.web.application.archival;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface ArchivalConfiguration
   extends ConfigurationComposite, Enabled
{
   @UseDefaults
   Property<Boolean> archiveDaily();
}
