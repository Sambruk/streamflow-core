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
package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;

/**
 * List of contacts for a case
 */
public class ContactsModel
   extends Observable
   implements Refreshable
{
   @Structure
   Module module;

   @Uses
   private CommandQueryClient client;

   ContactModel currentContact;

   TransactionList<ContactDTO> eventList = new TransactionList<ContactDTO>(new BasicEventList<ContactDTO>( ));

   public void refresh()
   {
      ResourceValue resource = client.query();
      ContactsDTO contactsDTO = (ContactsDTO) resource.index().get().buildWith().prototype();
      EventListSynch.synchronize( contactsDTO.contacts().get(), eventList );
      setChanged();
      notifyObservers( resource );
   }

   public EventList<ContactDTO> getEventList()
   {
      return eventList;
   }

   public void createContact()
   {
      client.postCommand("add", module.valueBuilderFactory().newValue(ContactDTO.class));
   }

   public void removeElement( int selectedIndex )
   {
      client.getSubClient( selectedIndex+"" ).delete();
   }

   public ContactModel newContactModel(int idx)
   {
      ContactDTO contact = eventList.get(idx);

      createInitialValues( contact );

      return  currentContact = module.objectBuilderFactory().newObjectBuilder(ContactModel.class).use( eventList.get(idx), client.getSubClient( ""+idx ) ).newInstance();
   }

   public ContactDTO createInitialValues( ContactDTO contact )
   {
      // Set empty initial values for phoneNumber, email and address.
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneDTO phone = module.valueBuilderFactory().newValue(ContactPhoneDTO.class).<ContactPhoneDTO>buildWith().prototype();
         contact.phoneNumbers().get().add( phone );
      }

      if (contact.addresses().get().isEmpty())
      {
         ContactAddressDTO address = module.valueBuilderFactory().newValue(ContactAddressDTO.class).<ContactAddressDTO>buildWith().prototype();
         contact.addresses().get().add( address );

      }

      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailDTO email = module.valueBuilderFactory().newValue(ContactEmailDTO.class).<ContactEmailDTO>buildWith().prototype();
         contact.emailAddresses().get().add( email );
      }
      return contact;
   }

   public ContactModel getCurrentContact()
   {
      return currentContact;
   }
}