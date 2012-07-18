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

import javax.swing.JTextField;

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.client.util.SuggestTextField;
import se.streamsource.streamflow.client.util.ValueBinder;

/**
 * Specific Class to handle the live search for Street Names. It should also
 * update the City attribute which explains the need for this class.
 * 
 * @author henrikreinhold
 * 
 * @param <T>
 */
public class StreetAddressSuggestTextField extends SuggestTextField<StreetSearchDTO>
{

   private static final long serialVersionUID = -1952912369783423979L;
   private StreetAddressSuggestModel model;
   private final ValueBinder addressViewBinder;
   private final JTextField cityField;

   public StreetAddressSuggestTextField(StreetAddressSuggestModel model, JTextField cityField,
         ValueBinder addressViewBinder)
   {
      super( model );
      this.model = model;
      this.cityField = cityField;
      this.addressViewBinder = addressViewBinder;

   }

   public void handleAcceptAction(StreetSearchDTO selectedItem)
   {
      getTextField().setText( selectedItem.address().get() );
      cityField.setText( selectedItem.area().get() );
      model.getContactModel().getAddress().address().set( selectedItem.address().get() );
      model.getContactModel().getAddress().city().set( selectedItem.area().get() );
      addressViewBinder.update( model.getContactModel().getAddress() );
      model.getContactModel().changeAddressAndCity( selectedItem.address().get(), selectedItem.area().get() );
   }

   public void handleSaveAction(String text)
   {
      if (text != null && !text.equals( model.getContactModel().getAddress().address().get() ))
      {
         model.getContactModel().changeAddress( text );
      }
   }

}
