/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.infrastructure.plugin.map;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import se.streamsource.streamflow.web.infrastructure.plugin.KartagoPluginConfiguration;

/**
 * Service that handles configuration of the Kartago Map client plugin
 */

@Mixins(KartagoMapService.Mixin.class)
public interface KartagoMapService extends ServiceComposite, Configuration, Activatable
{
   abstract class Mixin implements KartagoMapService, Activatable {
      
      @This
      Configuration<KartagoPluginConfiguration> config;
      
      public void activate() throws Exception
      {
         // Workaround to get automatic init values for this configuration property
         KartagoPluginConfiguration configuration = config.configuration();
      }

      public void passivate() throws Exception
      {
      }
   }
}
