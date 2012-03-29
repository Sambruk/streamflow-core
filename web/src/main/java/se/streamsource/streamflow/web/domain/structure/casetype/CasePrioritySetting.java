package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 *Composite for case priority settings.
 */
@Mixins( CasePrioritySetting.Mixin.class )
public interface CasePrioritySetting   
{
   void changeCasePrioritySetting(Boolean visible, Boolean mandatory );
   
   interface Data
   {
      @UseDefaults
      Property<Boolean> visible();
      
      @UseDefaults
      Property<Boolean> mandatory();
   }
   
   interface Events
   {
      void changedCasePrioritySetting( @Optional DomainEvent event, Boolean visible, Boolean show );
   }
   
   class Mixin
      implements CasePrioritySetting, Events
   {
      @This
      Data data;


      public void changeCasePrioritySetting( Boolean visible, Boolean mandatory )
      {
         // if there is no real change do nothing
         if( data.visible().get().equals( visible ) && data.mandatory().get().equals( mandatory ))
            return;
         
         changedCasePrioritySetting( null, visible, mandatory );
      }

      public void changedCasePrioritySetting( @Optional DomainEvent event, Boolean visible, Boolean mandatory )
      {
         data.visible().set( visible );
         data.mandatory().set( mandatory );
      }
   }
}
