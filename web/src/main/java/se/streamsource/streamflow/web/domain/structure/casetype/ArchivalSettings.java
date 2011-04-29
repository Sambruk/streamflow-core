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
