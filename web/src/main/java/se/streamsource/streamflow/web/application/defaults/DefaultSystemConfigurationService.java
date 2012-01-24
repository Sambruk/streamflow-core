package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

/**
 * A service holding system default configuration properties.
 */
@Mixins(DefaultSystemConfigurationService.Mixin.class)
public interface DefaultSystemConfigurationService
   extends ServiceComposite, Configuration
{

   public Configuration<DefaultSystemConfiguration> config();
   
   abstract class Mixin
      implements DefaultSystemConfigurationService
   {
      @This
      Configuration<DefaultSystemConfiguration> config;
      
      public Configuration<DefaultSystemConfiguration> config()
      {
         return config;
      }
   }
}
