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

import static org.jdesktop.application.Task.BlockingScope.COMPONENT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

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

   @Structure
   Module module;

   private ContactsModel model;

   private JList contacts;

   public ContactsView( @Service ApplicationContext context,
                        @Uses ContactsModel model)
   {
      super( new BorderLayout() );

      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setPreferredSize( new Dimension( 200, 0 ) );

      this.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      contacts = new JList(new EventListModel<ContactDTO>( model.getEventList() ));
      contacts.setPreferredSize( new Dimension( 200, 1000 ) );
      contacts.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      JScrollPane contactsScrollPane = new JScrollPane();
      contactsScrollPane.setViewportView( contacts );
      contacts.setCellRenderer( new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            ContactDTO contact = (ContactDTO) o;
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
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      model.addObserver( new RefreshComponents().visibleOn( "add", toolbar ) );

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

      new RefreshWhenShowing( this, this );
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
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( ((ContactDTO) contacts.getSelectedValue()).name().get() );
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

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames("changedStatus", "addedContact", "deletedContact", "updatedContact" ), transactions ))
      {
         model.refresh();

         if (Events.matches( Events.withNames("addedContact" ), transactions ))
            contacts.setSelectedIndex( contacts.getModel().getSize()-1 );
      }

   }

   public ContactsModel getModel()
   {
      return model;
   }
}