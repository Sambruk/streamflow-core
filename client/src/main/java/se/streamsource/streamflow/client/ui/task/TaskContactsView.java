/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

/**
 * JAVADOC
 */
public class TaskContactsView
      extends JPanel
{
   @Service
   UncaughtExceptionHandler exception;

   TaskContactsModel model;

   public JList contacts;
   private TaskContactView contactView;
   public EventListModel eventListModel;

   public TaskContactsView( @Service ApplicationContext context,
                            @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );
      setMaximumSize( new Dimension(150,1000) );

      contactView = obf.newObject( TaskContactView.class );

      contacts = new JList();
      contacts.setPreferredSize( new Dimension(150,1000) );
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

            String name = contact.name().get();
            String text = name;

            return super.getListCellRendererComponent( jList, text, i, b, b1 );
         }
      } );
      add( contactsScrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      contacts.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

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
   }

   @org.jdesktop.application.Action
   public void add() throws IOException, ResourceException
   {
      model.createContact();
      contacts.setSelectedIndex( model.getEventList().size() - 1 );
   }

   @org.jdesktop.application.Action
   public void remove() throws IOException, ResourceException
   {
      model.removeElement( getContactsList().getSelectedIndex() );
      contacts.clearSelection();
   }

   public JList getContactsList()
   {
      return contacts;
   }

   @Override
   public void setVisible( boolean aFlag )
   {
      super.setVisible( aFlag );

      if (aFlag)
         try
         {
            model.refresh();
            if (model.getEventList().size() > 0 && contacts.getSelectedIndex() == -1)
            {
               contacts.setSelectedIndex( 0 );
            }
         } catch (Exception e)
         {
            exception.uncaughtException( e );
         }
   }

   public void setModel( TaskContactsModel model )
   {
      this.model = model;
      if (eventListModel != null)
         eventListModel.dispose();
      eventListModel = new EventListModel(model.getEventList());
      contacts.setModel( eventListModel );
      if (model.getEventList().size() > 0 && isVisible())
      {
         contacts.setSelectedIndex( 0 );
      }
      if (isVisible())
      {
         setVisible( true );
      }

   }

   public TaskContactView getContactView()
   {
      return contactView;
   }

   public CommandQueryClient getTaskContactsResource()
   {
      return model.getTaskContactsClientResource();
   }
}