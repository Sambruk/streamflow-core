/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

/**
 * A service holding system default configuration properties.
 */
@Mixins(DefaultSystemConfigurationService.Mixin.class)
public interface DefaultSystemConfigurationService
   extends ServiceComposite, Configuration, Activatable
{

   public Configuration<DefaultSystemConfiguration> config();

   
   abstract class Mixin
      implements DefaultSystemConfigurationService, Activatable
   {
      @This
      Configuration<DefaultSystemConfiguration> config;

      public Configuration<DefaultSystemConfiguration> config()
      {
         return config;
      }

      public void activate() throws Exception
      {
         // Read arbitrary property just to activate config-handler
         config().configuration().enabled();
         
      }

   }
}
