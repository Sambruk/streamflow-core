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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.util.RefreshComponents;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;


/**
 * JAVADOC
 */
public class ContactsAdminView
      extends JPanel
{
   @Structure
   ValueBuilderFactory vbf;

   public ContactsAdminView( @Uses final ContactsModel model, @Structure final ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );

      final ContactsView contactsView = obf.newObjectBuilder( ContactsView.class ).use( model ).newInstance();
      final ContactView contactView = obf.newObject( ContactView.class );

      contactsView.getModel().addObserver( new RefreshComponents().enabledOn( "add", contactView ) );

      add( contactsView, BorderLayout.WEST );
      add( contactView, BorderLayout.CENTER );

      final JList list = contactsView.getContactsList();
      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx != -1)
               {
                  ContactModel contactModel = model.newContactModel(idx);
                  contactView.setModel( contactModel );

                  contactView.setFocusOnName();
               } else
               {
                  contactView.setModel( null );
               }
            }
         }
      } );

   }
}