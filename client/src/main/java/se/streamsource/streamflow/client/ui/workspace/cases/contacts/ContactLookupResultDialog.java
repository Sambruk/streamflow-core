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
package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;

public class ContactLookupResultDialog extends JPanel
{
   public JXTable contactTable;

   private ContactDTO selectedContact;

   public ContactLookupResultDialog( @Service ApplicationContext context, @Uses List<ContactDTO> contacts )
   {
      contactTable = new JXTable();
      contactTable.setModel( new ContactLookupResultFieldsValueModel( contacts ) );
      contactTable.setPreferredScrollableViewportSize( new Dimension( 400, 120 ) );

      setLayout( new BorderLayout() );
      setActionMap( context.getActionMap( this ) );

      add( new JScrollPane( contactTable ), BorderLayout.CENTER );
   }

   @Action
   public void execute()
   {

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public ContactDTO getSelectedContact()
   {
      int selectedRow = contactTable.getSelectedRow();
      if (selectedRow == -1)
         return null;
      else
         return ((ContactLookupResultFieldsValueModel) contactTable.getModel()).getContactValueAt( contactTable.getSelectedRow() );
   }
}
