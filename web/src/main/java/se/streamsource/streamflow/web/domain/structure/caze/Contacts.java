/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.caze;

import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Contacts.Mixin.class)
public interface Contacts
{
   public void addContact( ContactDTO newContact );

   public void updateContact( int index, ContactDTO contact );

   public void deleteContact( int index );

   public boolean hasContacts();

   interface Data
   {
      @UseDefaults
      Property<List<ContactDTO>> contacts();

      void addedContact( @Optional DomainEvent event, ContactDTO newContact );

      void updatedContact( @Optional DomainEvent event, int index, ContactDTO contact );

      void deletedContact( @Optional DomainEvent event, int index );
   }

   abstract class Mixin
         implements Contacts, Data
   {
      @This
      Data state;

      @Structure
      Module module;

      public void addContact( ContactDTO newContact )
      {
         addedContact( null, newContact );
      }

      public void updateContact( int index, ContactDTO contact )
      {
         if (contacts().get().size() > index)
         {
            updatedContact( null, index, contact );
         }
      }

      public void deleteContact( int index )
      {
         if (contacts().get().size() > index)
         {
            deletedContact( null, index );
         }
      }

      public void addedContact( DomainEvent event, ContactDTO newContact )
      {
         List<ContactDTO> contacts = state.contacts().get();
         contacts.add( newContact );
         state.contacts().set( contacts );
      }

      public void updatedContact( DomainEvent event, int index, ContactDTO contact )
      {
         List<ContactDTO> contacts = state.contacts().get();
         contacts.set( index, contact );
         state.contacts().set( contacts );
      }

      public void deletedContact( DomainEvent event, int index )
      {
         List<ContactDTO> contacts = state.contacts().get();
         contacts.remove( index );
         state.contacts().set( contacts );
      }

      public boolean hasContacts()
      {
         return !state.contacts().get().isEmpty() && !isTemplate( state.contacts().get().get(0) );
      }

      private boolean isTemplate( ContactDTO contact )
      {
         return "".equals( contact.name().get() )
               && contact.addresses().get().isEmpty()
               && "".equals( contact.company().get() )
               && "".equals( contact.contactId().get() )
               && contact.emailAddresses().get().isEmpty()
               && "".equals( contact.note().get())
               && !contact.isCompany().get()
               && contact.phoneNumbers().get().isEmpty()
               && "".equals( contact.picture().get());

      }
   }

}