/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.domain.contact;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Contactable.Mixin.class)
public interface Contactable
{
   void updateContact( ContactValue contact );

   ContactValue getContact();

   interface Data
   {
      Property<ContactValue> contact();

      void updatedContact( DomainEvent event, ContactValue contact );
   }

   abstract class Mixin
         implements Contactable, Data
   {
      @This
      Data state;

      @This
      Describable describable;

      public void updateContact( ContactValue newContact )
      {
         updatedContact( DomainEvent.CREATE, newContact );
      }

      public ContactValue getContact()
      {
         return state.contact().get();
      }

      public void updatedContact( DomainEvent event, ContactValue contact )
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
