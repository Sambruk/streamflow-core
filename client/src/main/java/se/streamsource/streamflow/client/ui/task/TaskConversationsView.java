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

package se.streamsource.streamflow.client.ui.task;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.task.conversations.ConversationsListCellRenderer;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;

public class TaskConversationsView
      extends JSplitPane
      implements ListEventListener
{

   @Structure
   ObjectBuilderFactory obf;

   private TaskConversationsModel model;
   private RefreshWhenVisible refresher;

   private JList list;

   private ActionMap am;
   private ApplicationContext context;

   public TaskConversationsView( @Service final ApplicationContext context )
   {

      am = context.getActionMap( this );
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
      list.setFixedCellHeight(-1);
      list.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
      
      list.addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            TaskConversationModel conversationModel = obf.newObjectBuilder( TaskConversationModel.class ).use( model.client.getSubClient( "tobechangedToEntityRef" ) ).newInstance();
            TaskConversationView conversationView = obf.newObjectBuilder( TaskConversationView.class ).use( context, obf ).newInstance();
            conversationView.setModel( conversationModel );

            setRightComponent( conversationView );

            ((CardLayout) conversationView.getLayout()).show( conversationView, "VIEW" );
         }
      } );

      JScrollPane scroll = new JScrollPane( list );
      left.add( scroll, BorderLayout.CENTER );

      JPanel addPanel = new JPanel();
      JButton addConversation = new JButton( am.get( "add" ) );
      addPanel.add( addConversation, FlowLayout.LEFT );
      left.add( addPanel, BorderLayout.SOUTH );

      setLeftComponent( left );


      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   @Action
   public void add() throws ResourceException, IOException
   {
      //Todo Add a new conversation
      TaskConversationModel conversationModel = obf.newObjectBuilder( TaskConversationModel.class ).use( model.client.getSubClient( "tobechangedToEntityRef" ) ).newInstance();
      TaskConversationView conversationView = obf.newObjectBuilder( TaskConversationView.class ).use( context, obf ).newInstance();
      conversationView.setModel( conversationModel );

      setRightComponent( conversationView );

      ((CardLayout) conversationView.getLayout()).show( conversationView, "NEW" );
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
         taskConversationsModel.conversations().addListEventListener( this );

         model.conversations().addListEventListener( this );
         listChanged( null );
      }

   }

   public void listChanged( ListEvent listEvent )
   {
      list.removeAll();
//      list.setModel(model)
      list.setModel( new EventListModel<ConversationDTO>(model.conversations()) );
      list.revalidate();
   }
}
