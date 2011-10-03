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

package se.streamsource.streamflow.web.context.administration;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.api.administration.ArchivalSettingsDTO;
import se.streamsource.streamflow.web.domain.structure.casetype.ArchivalSettings;

/**
 * TODO
 */
public class ArchivalSettingsContext
   implements IndexContext<ArchivalSettingsDTO>, UpdateContext<ArchivalSettingsDTO>
{
   public ArchivalSettingsDTO index()
   {
      return RoleMap.role(ArchivalSettings.Data.class).archivalSettings().get();
   }

   public void update(ArchivalSettingsDTO value)
   {
      RoleMap.role(ArchivalSettings.class).changeArchivalSettings(value);
   }
}
