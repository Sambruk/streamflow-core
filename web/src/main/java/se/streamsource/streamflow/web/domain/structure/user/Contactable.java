/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * JAVADOC
 */
@Mixins(Contactable.Mixin.class)
public interface Contactable
{
   void updateContact( ContactDTO contact );

   ContactDTO getContact();

   interface Data
   {
      Property<ContactDTO> contact();

      void updatedContact( @Optional DomainEvent event, ContactDTO contact );
   }

   abstract class Mixin
         implements Contactable, Data
   {
      @This
      Data state;

      @This
      Describable describable;

      public void updateContact( ContactDTO newContact )
      {
         updatedContact( null, newContact );
      }

      public ContactDTO getContact()
      {
         return state.contact().get();
      }

      public void updatedContact( DomainEvent event, ContactDTO contact )
      {
         state.contact().set( contact );

         // Change description
         if (!contact.name().get().equals(""))
         {
            describable.changeDescription( contact.name().get() );
         }
      }
   }
}
