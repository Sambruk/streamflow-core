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
