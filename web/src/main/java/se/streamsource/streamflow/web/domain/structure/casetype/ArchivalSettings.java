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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.api.administration.ArchivalSettingsDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Settings for archival of cases
 */
@Mixins(ArchivalSettings.Mixin.class)
public interface ArchivalSettings
{
   void changeArchivalSettings(ArchivalSettingsDTO settings);

   interface Data
   {
      @Optional
      Property<ArchivalSettingsDTO> archivalSettings();
   }

   interface Events
   {
      void changedArchivalSettings(@Optional DomainEvent event, ArchivalSettingsDTO settings);
   }

   class Mixin
      implements Events, ArchivalSettings
   {
      @This
      Data data;

      public void changeArchivalSettings(ArchivalSettingsDTO settings)
      {
         changedArchivalSettings(null, settings);
      }

      public void changedArchivalSettings(@Optional DomainEvent event, ArchivalSettingsDTO settings)
      {
         data.archivalSettings().set(settings);
      }
   }
}
