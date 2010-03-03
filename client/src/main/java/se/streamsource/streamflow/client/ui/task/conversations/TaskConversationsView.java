/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task.conversations;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.io.IOException;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class TaskConversationsView
      extends JSplitPane
      implements ListEventListener
{
   @Uses
   Iterable<NameDialog> topicDialogs;

   @Service
   DialogService dialogs;

   private TaskConversationsModel model;
   private RefreshWhenVisible refresher;

   private JList list;

   private ApplicationContext context;

   public TaskConversationsView( @Service final ApplicationContext context, @Structure ObjectBuilderFactory obf )
   {

      setActionMap(context.getActionMap( this ));
      MacOsUIWrapper.convertAccelerators( getActionMap() );
      this.context = context;
      JPanel left = new JPanel( new BorderLayout() );
      final CardLayout cards = new CardLayout();
      final JPanel right = new JPanel( cards );
      JPanel empty = new JPanel();
      right.add( empty, "EMPTY" );

      setRightComponent( right );

      cards.show( right, "EMPTY" );

      list = new JList();
      list.setCellRenderer( new ConversationsListCellRenderer() );
      list.setFixedCellHeight( -1 );
      list.getSelectionModel().setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION );

      final TaskConversationView conversationView = obf.newObjectBuilder( TaskConversationView.class ).use( context, obf ).newInstance();
      list.addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            if (list.getSelectedIndex() != -1 && !e.getValueIsAdjusting())
            {
               conversationView.setModel( model.conversationModels.get( ((LinkValue) list.getSelectedValue()).href().get() ) );
               setRightComponent( conversationView );
            } else
            {
               setRightComponent( right );
               cards.show( right, "EMPTY" );
            }
         }
      } );

      JScrollPane scroll = new JScrollPane( list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      left.add( scroll, BorderLayout.CENTER );

      JPanel addPanel = new JPanel();
      javax.swing.Action addConversationAction = getActionMap().get( "addConversation" );
      JButton addConversation = new JButton( addConversationAction );
      addConversation.registerKeyboardAction( addConversationAction, (KeyStroke) addConversationAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      addPanel.add( addConversation, FlowLayout.LEFT );
      left.add( addPanel, BorderLayout.SOUTH );

      setLeftComponent( left );
      this.setDividerLocation( 200 );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   @Action
   public void addConversation() throws ResourceException, IOException
   {
      NameDialog dialog = topicDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( TaskResources.new_conversation_topic ) );

      if (dialog.name() != null )
      {
         model.createConversation( dialog.name() );
         list.setSelectedIndex( model.conversations().size() - 1 );
      }
   }

   public void setModel( TaskConversationsModel taskConversationsModel )
   {
      if (model != null)
         model.conversations().removeListEventListener( this );

      model = taskConversationsModel;
      model.refresh();
      refresher.setRefreshable( model );

      if (model != null)
      {
         model.conversations().addListEventListener( this );
         listChanged( null );
      }

   }

   public void listChanged( ListEvent listEvent )
   {
      int prevSelected = list.getSelectedIndex();
      list.setModel( new EventListModel<ConversationDTO>( model.conversations() ) );
      list.repaint();
      list.setSelectedIndex( prevSelected );
   }
}
