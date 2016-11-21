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
package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: henrikreinhold
 * Date: Sep 8, 2010
 * Time: 8:12:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContactLookupResultFieldsValueModel extends AbstractTableModel
{
   String[] columnNames = {
         i18n.text( WorkspaceResources.name_label ),
         i18n.text( WorkspaceResources.contact_id_label ),
         i18n.text( WorkspaceResources.phone_label ),
         i18n.text( WorkspaceResources.email_label )
   };


   List<ContactDTO> contacts = Collections.emptyList();

   public ContactLookupResultFieldsValueModel( List<ContactDTO> contacts )
   {
      this.contacts = contacts;
   }

   public int getRowCount()
   {
      return contacts == null ? 0 : contacts.size();
   }

   public int getColumnCount()
   {
      return columnNames.length;
   }

   public Object getValueAt( int row, int col )
   {
      ContactDTO value = contacts.get( row );

      switch (col)
      {
         case 0:
            return value.name().get();
         case 1:
            return value.contactId().get();
         case 2:
            if (value.phoneNumbers().get().isEmpty())
               return "";
            else
               return value.phoneNumbers().get().get( 0 ).phoneNumber();
         case 3:
            if (value.emailAddresses().get().isEmpty())
               return "";
            else
               return value.emailAddresses().get().get( 0 ).emailAddress();
      }
      return null;
   }

   @Override
   public boolean isCellEditable( int rowIndex, int columnIndex )
   {
      return false;
   }

   @Override
   public String getColumnName( int i )
   {
      return columnNames[i];
   }

   public ContactDTO getContactValueAt( int index )
   {
      try
      {
         return contacts.get( index );
      } catch (IndexOutOfBoundsException e)
      {
         return null;
      }
   }
}
