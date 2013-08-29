/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.caze;

import org.joda.time.DateTime;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Provides the means to trace the last notification message sent for a case.
 */
@Mixins(NotificationTrace.Mixin.class)
public interface NotificationTrace
{
   void updateNotificationTrace( DateTime dateOn, String name);

   DateTime getNotifiedOn();

   String getEventName();

   interface Data
   {
      @Optional
      Property<DateTime> notifiedOn();

      @Optional
      Property<String>  eventName();

      void updatedNotificationTrace( @Optional DomainEvent event, DateTime dateOn, String name );
   }

   abstract class Mixin
      implements NotificationTrace, Data
   {
      @This
      Data state;

      public void updatedNotificationTrace( @Optional DomainEvent event, DateTime dateOn, String name )
      {
         state.notifiedOn().set( dateOn );
         state.eventName().set( name );
      }

      public void updateNotificationTrace( DateTime dateOn, String name )
      {
         if( !dateOn.equals( state.notifiedOn().get() ) || !name.equals( state.eventName().get() ) )
         {
            updatedNotificationTrace( null, dateOn, name );
         }
      }

      public DateTime getNotifiedOn()
      {
         return state.notifiedOn().get();
      }

      public String getEventName()
      {
         return state.eventName().get();
      }
   }
}
