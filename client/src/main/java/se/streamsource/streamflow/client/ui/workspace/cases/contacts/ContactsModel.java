/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.resource.caze.*;

import java.util.*;

/**
 * List of contacts for a case
 */
public class ContactsModel
   extends Observable
   implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   TransactionList<ContactValue> eventList = new TransactionList<ContactValue>(new BasicEventList<ContactValue>( ));

   public void refresh()
   {
      ResourceValue resource = client.queryResource();
      ContactsDTO contactsDTO = (ContactsDTO) resource.index().get().buildWith().prototype();
      EventListSynch.synchronize( contactsDTO.contacts().get(), eventList );
      setChanged();
      notifyObservers( resource );
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