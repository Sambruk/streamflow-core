package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.RangeConstraint;
import org.qi4j.library.constraints.annotation.Range;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Settings for archival of cases
 */
@Mixins(ArchivalSettings.Mixin.class)
public interface ArchivalSettings
{
   enum ArchivalType
   {
      delete,
      export
   }

   void changeMaxAge(@Range(min = 0, max = Double.MAX_VALUE) Integer maxAge);
   void changeArchivalType(ArchivalType archivalType);

   interface Data
   {
      @UseDefaults
      Property<Integer> maxAge();

      @UseDefaults
      Property<ArchivalType> archivalType();
   }

   interface Events
   {
      void changedMaxAge(@Optional DomainEvent event, int maxAge);
      void changedArchivalType(@Optional DomainEvent event, ArchivalType newArchivalType);
   }

   class Mixin
      implements Events, ArchivalSettings
   {
      @This
      Data data;

      public void changeMaxAge(Integer maxAge)
      {
         changedMaxAge(null, maxAge);
      }

      public void changeArchivalType(ArchivalType archivalType)
      {
         changedArchivalType(null, archivalType);
      }

      public void changedMaxAge(@Optional DomainEvent event, int maxAge)
      {
         data.maxAge().set(maxAge);
      }

      public void changedArchivalType(@Optional DomainEvent event, ArchivalType newArchivalType)
      {
         data.archivalType().set(newArchivalType);
      }
   }
}
