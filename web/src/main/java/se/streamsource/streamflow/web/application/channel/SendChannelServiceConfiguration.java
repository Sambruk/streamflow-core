package se.streamsource.streamflow.web.application.channel;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTrackerConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

/**
 * Configuration for integration channel service.
 */
public interface SendChannelServiceConfiguration
      extends ConfigurationComposite, Enabled, TransactionTrackerConfiguration, PluginConfiguration
{
}
