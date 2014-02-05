/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.client.util.SuggestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


public class StreetAddressSuggestModel extends Observable implements SuggestModel<StreetSearchDTO>
{
   private ContactModel contactModel;
   private StreetsDTO streets;

   public List<StreetSearchDTO> options(String searchString)
   {
      if (contactModel.isStreetLookupEnabled()) 
      {
         streets = contactModel.searchStreets( searchString );
         return streets.streets().get();
      } else
      {
         return new ArrayList<StreetSearchDTO>();
      }
   }

   public String displayValue(StreetSearchDTO value)
   {
      return value.address().get() + ", " + value.area().get();
   }

   public String displayValueAt(int index)
   {
      return displayValue( streets.streets().get().get( index ) );
   }

   public void setContactModel(ContactModel contactModel)
   {
      this.contactModel = contactModel;
      setChanged();
      notifyObservers();
   }

   public StreetSearchDTO valueAt(int index)
   {
      return streets.streets().get().get( index );
   }
   
   public ContactModel getContactModel()
   {
      return contactModel;
   }
}
