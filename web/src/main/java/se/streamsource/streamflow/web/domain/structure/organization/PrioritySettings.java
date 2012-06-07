package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Contains settings for priority
 */
@Mixins( PrioritySettings.Mixin.class )
public interface PrioritySettings
{
   void changeColor( @Optional String newColor );
   void changePriority( Integer newPriority );

   interface Data
   {
      @Optional
      Property<String> color();

      Property<Integer> priority();
   }

   interface Event
   {
      void changedColor( @Optional DomainEvent event, String newColor );
      void changedPriority( @Optional DomainEvent event, Integer newPriority );
   }

   abstract class Mixin
      implements PrioritySettings, Event
   {
      @This
      Data data;

      public void changeColor( @Optional String newColor )
      {
         if(((data.color().get() != null) && !data.color().get().equals( newColor ))
               || ((data.color().get() == null) && (newColor != null)))
         {
            changedColor( null, newColor );
         }
      }

      public void changePriority( Integer newPriority )
      {
         changedPriority( null, newPriority );
      }
   }
}
