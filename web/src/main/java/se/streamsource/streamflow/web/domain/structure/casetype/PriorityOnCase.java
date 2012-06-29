/**
 *
 * Copyright 2009-2012 Streamsource AB
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
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;

/**
 * Composite for case priority settings.
 */
@Mixins( PriorityOnCase.Mixin.class )
public interface PriorityOnCase
{
   void changeVisibility( Boolean visible );
   void changeMandatory( Boolean mandatory );
   void changeDefaultPriority( @Optional Priority priority );



   interface Data
   {
      @UseDefaults
      Property<Boolean> visible();
      
      @UseDefaults
      Property<Boolean> mandatory();

      @Optional
      @Aggregated
      Association<Priority> defaultPriority();

      void changedVisibility( @Optional DomainEvent event, Boolean visible );
      void changedMandatory( @Optional DomainEvent event, Boolean mandatory );
      void changedDefaultPriority( @Optional DomainEvent event, @Optional Priority defaultPriority );
   }

   abstract class Mixin
      implements PriorityOnCase, Data
   {
      @This
      Data data;


      public void changeVisibility( Boolean visible )
      {
         // if there is no real change do nothing
         if( data.visible().get().equals( visible ) )
            return;
         
         data.changedVisibility( null, visible );
      }

      public void changeMandatory( Boolean mandatory )
      {
         // if there is no real change do nothings
         if( data.mandatory().get().equals( mandatory ) )
            return;

         data.changedMandatory( null, mandatory );
      }

      public void changeDefaultPriority( Priority defaultPriority )
      {
         if( (defaultPriority != null && defaultPriority.equals( data.defaultPriority().get() ))
               || (defaultPriority == null && data.defaultPriority().get() == null ))
            return;
         data.changedDefaultPriority( null, defaultPriority );
      }

      public void changedVisibility( DomainEvent event, Boolean visible)
      {
         data.visible().set( visible );
      }

      public void changedDefaultPriority( DomainEvent event, Priority defaultPriority)
      {
         data.defaultPriority().set( defaultPriority );
      }
   }
}
