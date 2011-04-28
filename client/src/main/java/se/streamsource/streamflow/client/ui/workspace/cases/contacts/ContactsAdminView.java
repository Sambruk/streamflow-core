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
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;


/**
 * JAVADOC
 */
public class ContactsAdminView
      extends JPanel
{
   @Structure
   ValueBuilderFactory vbf;

   public ContactsAdminView( @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );

      final ContactsView contactsView = obf.newObjectBuilder( ContactsView.class ).use( client ).newInstance();
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
                  ContactValue contactValue = (ContactValue) list.getModel().getElementAt( idx );
                  // Set empty initial values for phoneNumber, email and address.
                  if (contactValue.phoneNumbers().get().isEmpty())
                  {
                     ContactPhoneValue phone = vbf.newValue( ContactPhoneValue.class ).<ContactPhoneValue>buildWith().prototype();
                     contactValue.phoneNumbers().get().add( phone );

                  }

                  if (contactValue.addresses().get().isEmpty())
                  {
                     ContactAddressValue address = vbf.newValue( ContactAddressValue.class ).<ContactAddressValue>buildWith().prototype();
                     contactValue.addresses().get().add( address );

                  }

                  if (contactValue.emailAddresses().get().isEmpty())
                  {
                     ContactEmailValue email = vbf.newValue( ContactEmailValue.class ).<ContactEmailValue>buildWith().prototype();
                     contactValue.emailAddresses().get().add( email );

                  }

                  ContactModel contactModel = obf.newObjectBuilder( ContactModel.class ).use( contactValue, client.getSubClient( ""+idx ) ).newInstance();
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