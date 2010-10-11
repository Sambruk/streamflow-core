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

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;

import com.jgoodies.forms.factories.Borders;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import static org.jdesktop.application.Task.BlockingScope.COMPONENT;

/**
 * JAVADOC
 */
public class ContactsView
      extends JPanel
   implements TransactionListener, Refreshable
{
   @Service
   UncaughtExceptionHandler exception;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private ContactsModel model;

   private JList contacts;

   public ContactsView( @Service ApplicationContext context,
                        @Uses CommandQueryClient client,
                        @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );

      model = obf.newObjectBuilder( ContactsModel.class ).use(client).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setPreferredSize( new Dimension( 200, 0 ) );

      this.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      contacts = new JList(new EventListModel<ContactValue>( model.getEventList() ));
      contacts.setPreferredSize( new Dimension( 200, 1000 ) );
      contacts.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      JScrollPane contactsScrollPane = new JScrollPane();
      contactsScrollPane.setViewportView( contacts );
      contacts.setCellRenderer( new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            ContactValue contact = (ContactValue) o;
            if ("".equals( contact.name().get() ))
            {
               Component cell = super.getListCellRendererComponent( jList, i18n.text( WorkspaceResources.name_label ), i, b, b1 );
               cell.setForeground( Color.GRAY );
               return cell;
            }

            String text = contact.name().get();

            return super.getListCellRendererComponent( jList, text, i, b, b1 );
         }
      } );
      add( contactsScrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      contacts.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

/*
      setFocusTraversalPolicy( new LayoutFocusTraversalPolicy() );
      setFocusCycleRoot( true );
      setFocusable( true );
      addFocusListener( new FocusListener()
      {
         public void focusGained( FocusEvent e )
         {
            Component defaultComp = getFocusTraversalPolicy().getDefaultComponent( contactView );
            if (defaultComp != null)
            {
               defaultComp.requestFocusInWindow();
            }
         }

         public void focusLost( FocusEvent e )
         {
         }
      } );
*/

      new RefreshWhenVisible( this, this );
   }

   @org.jdesktop.application.Action(block = COMPONENT)
   public Task add() throws IOException, ResourceException
   {
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.createContact();
         }
      };
   }

   @org.jdesktop.application.Action(block = COMPONENT)
   public Task remove() throws IOException, ResourceException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( ((ContactValue) contacts.getSelectedValue()).name().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.removeElement( getContactsList().getSelectedIndex() );
            }
         };
      } else
         return null;
   }

   public JList getContactsList()
   {
      return contacts;
   }

   public void refresh()
   {
      model.refresh();

      if (model.getEventList().size() > 0 && contacts.getSelectedIndex() == -1)
      {
         contacts.setSelectedIndex( 0 );
      }
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, Events.withNames("addedContact", "deletedContact", "updatedContact" )))
      {
         model.refresh();

         if (Events.matches( transactions, Events.withNames("addedContact" )))
            contacts.setSelectedIndex( contacts.getModel().getSize()-1 );
      }

   }
}