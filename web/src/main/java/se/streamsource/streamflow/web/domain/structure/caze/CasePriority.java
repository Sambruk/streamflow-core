package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Contains case priority information for a case.
 */
@Mixins(CasePriority.Mixin.class)
public interface CasePriority
{
   void changePriority( @Optional CasePriorityValue priority );
   interface Data
   {
      @Optional
      Property<CasePriorityValue> priority();
   }
   
   interface Events
   {
      void changedPriority( @Optional DomainEvent event, @Optional CasePriorityValue priority );
   }

   class Mixin
      implements CasePriority, Events
   {
      @This
      Data data;

      public void changePriority( @Optional CasePriorityValue priority )
      {
         // check if there would actually be a change before changing
         if( (data.priority().get() == null && priority == null) ||
               ( priority != null && priority.equals( data.priority().get() )))
            return;
         
         changedPriority( null, priority );
      }

      public void changedPriority( @Optional DomainEvent event, @Optional CasePriorityValue priority )
      {
         data.priority().set( priority );
      }
   }
}
