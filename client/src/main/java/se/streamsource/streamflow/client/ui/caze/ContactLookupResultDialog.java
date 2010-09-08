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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.domain.contact.ContactValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

public class ContactLookupResultDialog extends JPanel
{
   public JXTable contactList;

   public ContactLookupResultDialog( @Service ApplicationContext context, @Uses List<ContactValue> contacts )
   {
      contactList = new JXTable();
      contactList.setModel( new ContactLookupResultFieldsValueModel( contacts ) );

      setLayout( new BorderLayout() );
      setActionMap( context.getActionMap( this ) );

      add( contactList, BorderLayout.CENTER );
   }

   @Action
   public void execute()
   {
      // TODO Get the selected Contact from the Table

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
