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
package se.streamsource.streamflow.web.domain.entity;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Holds the external reference for an entity.
 */
@Mixins( ExternalReference.Mixin.class )
public interface ExternalReference
{
   void changeReference( @Optional String reference );

   interface Data
   {
      @Optional
      Property<String> reference();

      void changedReference( @Optional DomainEvent event, @Optional String reference );
   }

   abstract class Mixin
      implements ExternalReference, Data
   {
      @This
      Data data;

      public void changedReference( DomainEvent event, String reference )
      {
         data.reference().set( reference );
      }

      public void changeReference( String reference )
      {
         changedReference( null, reference );
      }
   }
}
