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

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.api.workspace.cases.location.CaseAddressDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Contains location information for a case.
 */
@Mixins(Location.Mixin.class)
public interface Location
{
   void changeLocation(@Optional String location);

   void changeAdress(CaseAddressDTO address);

   interface Data
   {
      @Optional
      Property<String> location();

      @Optional
      Property<CaseAddressDTO> address();

      void changedLocation(@Optional DomainEvent event, @Optional String location);

      void changedAddress(@Optional DomainEvent event, CaseAddressDTO address);
   }

   abstract class Mixin implements Location, Data
   {
      @This
      Data data;

      public void changeLocation(@Optional String location)
      {
         // check if there would actually be a change before changing
         if ((data.location().get() == null && location == null)
               || (location != null && location.equals( data.location().get() )))
            return;

         data.changedLocation( null, location );
      }

      public void changedLocation(DomainEvent event, String location)
      {
         data.location().set( location );
      }

      public void changeAddress(CaseAddressDTO address)
      {
         data.changedAddress( null, address );
      }

      public void changedAddress(DomainEvent event, CaseAddressDTO address)
      {
         data.address().set( address );
      }
   }
}
