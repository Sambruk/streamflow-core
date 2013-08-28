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
