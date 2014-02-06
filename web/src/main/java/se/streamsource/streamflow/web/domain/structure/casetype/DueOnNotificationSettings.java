/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.api.administration.DueOnNotificationSettingsDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Settings for DueOn notifications of cases
 */
@Mixins(DueOnNotificationSettings.Mixin.class)
public interface DueOnNotificationSettings
{
   void activateNotifications( Boolean activate);
   void changeNotificationThreshold( Integer days );
   void addRecipient( EntityReference recipient );
   void removeRecipient( EntityReference recipient );

   interface Data
   {
      @Optional
      Property<DueOnNotificationSettingsDTO> notificationSettings();
   }

   interface Events
   {
      void changedNotificationSettings(@Optional DomainEvent event, DueOnNotificationSettingsDTO settings);
   }

   class Mixin
      implements Events, DueOnNotificationSettings
   {
      @This
      Data data;

      @Structure
      Module module;
      
      public void activateNotifications( Boolean activate)
      {
         ValueBuilder<DueOnNotificationSettingsDTO> builder = getBuilder();
         builder.prototype().active().set( activate );
         changedNotificationSettings( null, builder.newInstance() );
      }

      public void changeNotificationThreshold( Integer days )
      {
         ValueBuilder<DueOnNotificationSettingsDTO> builder = getBuilder();
         builder.prototype().threshold().set( days );
         changedNotificationSettings( null, builder.newInstance() );
      }
      
      public void addRecipient( EntityReference recipient )
      {
         ValueBuilder<DueOnNotificationSettingsDTO> builder = getBuilder();
         if (!data.notificationSettings().get().additionalrecipients().get().contains( recipient ))
         {
            builder.prototype().additionalrecipients().get().add( recipient );
            changedNotificationSettings( null, builder.newInstance() );
         }
      }
      
      public void removeRecipient( EntityReference recipient )
      {
         ValueBuilder<DueOnNotificationSettingsDTO> builder = getBuilder();
         builder.prototype().additionalrecipients().get().remove( recipient );
         changedNotificationSettings( null, builder.newInstance() );  
      }

      public void changedNotificationSettings(@Optional DomainEvent event, DueOnNotificationSettingsDTO settings)
      {
         data.notificationSettings().set(settings);
      }
      
      private ValueBuilder<DueOnNotificationSettingsDTO> getBuilder()
      {
         if (data.notificationSettings().get() == null)
         {
            return module.valueBuilderFactory().newValueBuilder( DueOnNotificationSettingsDTO.class );
         } else
         {
            return module.valueBuilderFactory().newValueBuilder( DueOnNotificationSettingsDTO.class ).withPrototype( data.notificationSettings().get() );
         }
      }
   }
}
