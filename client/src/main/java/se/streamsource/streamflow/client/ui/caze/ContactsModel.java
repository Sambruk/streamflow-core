/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.caze.ContactsDTO;

/**
 * List of contacts for a case
 */
public class ContactsModel
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   TransactionList<ContactValue> eventList = new TransactionList<ContactValue>(new BasicEventList<ContactValue>( ));

   public void refresh()
   {
      ContactsDTO contactsDTO = (ContactsDTO) client.query( "contacts", ContactsDTO.class ).buildWith().prototype();
      EventListSynch.synchronize( contactsDTO.contacts().get(), eventList );
   }

   public EventList<ContactValue> getEventList()
   {
      return eventList;
   }

   public void createContact()
   {
      client.postCommand( "add", vbf.newValue( ContactValue.class ) );
   }

   public void removeElement( int selectedIndex )
   {
      client.getSubClient( selectedIndex+"" ).delete();
   }
}