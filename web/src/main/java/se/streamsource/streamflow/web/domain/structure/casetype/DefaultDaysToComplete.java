package se.streamsource.streamflow.web.domain.structure.casetype;

import apple.awt.ClientPropertyApplicator;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.GreaterThan;
import org.qi4j.library.constraints.annotation.Range;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * TODO
 */
@Mixins(DefaultDaysToComplete.Mixin.class)
public interface DefaultDaysToComplete
{
   void changeDefaultDaysToComplete(@Range(min=0, max=Double.MAX_VALUE) Integer defaultDaysToComplete);

   interface Data
   {
      @UseDefaults
      Property<Integer> defaultDaysToComplete();
   }

   interface Events
   {
      void changedDefaultDaysToComplete(@Optional DomainEvent event, int defaultDaysToComplete);
   }

   class Mixin
      implements DefaultDaysToComplete, Events
   {
      @This Data data;

      public void changeDefaultDaysToComplete(Integer defaultDaysToComplete)
      {
         changedDefaultDaysToComplete(null, defaultDaysToComplete);
      }

      public void changedDefaultDaysToComplete(@Optional DomainEvent event, int defaultDaysToComplete)
      {
         data.defaultDaysToComplete().set(defaultDaysToComplete);
      }
   }
}
