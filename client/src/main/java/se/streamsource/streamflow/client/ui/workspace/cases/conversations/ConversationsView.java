/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.NameDialog;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.util.Strings;

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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class ConversationsView
      extends JSplitPane
      implements TransactionListener
{
   @Uses
   Iterable<NameDialog> topicDialogs;

   @Service
   DialogService dialogs;

   private ConversationsModel model;

   private JList list;

   public ConversationsView( @Service final ApplicationContext context, @Structure final ObjectBuilderFactory obf, @Uses final CommandQueryClient client )
   {
      model = obf.newObjectBuilder(ConversationsModel.class ).use( client ).newInstance();

      setActionMap(context.getActionMap( this ));
      MacOsUIWrapper.convertAccelerators( getActionMap() );
      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      JPanel left = new JPanel( new BorderLayout() );
      left.setPreferredSize( new Dimension(200, 100) );
      final CardLayout cards = new CardLayout();
      final JPanel right = new JPanel( cards );
      JPanel empty = new JPanel();
      right.add( empty, "EMPTY" );

      setRightComponent( right );

      cards.show( right, "EMPTY" );

      list = new JList();
      list.setModel( new EventListModel<ConversationDTO>( model.conversations() ) );
      list.setCellRenderer( new ConversationsListCellRenderer() );
      list.setFixedCellHeight( -1 );
      list.getSelectionModel().setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION );

      list.addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            if (list.getSelectedIndex() != -1 && !e.getValueIsAdjusting())
            {
               final ConversationView conversationView = obf.newObjectBuilder( ConversationView.class ).use( client.getClient( (LinkValue) list.getSelectedValue() )).newInstance();
               setRightComponent( conversationView );
            } else
            {
               setRightComponent( right );
               cards.show( right, "EMPTY" );
            }
         }
      } );

      JScrollPane scroll = new JScrollPane( list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      scroll.setMinimumSize( new Dimension(250, 100) );
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
      this.setDividerLocation( -1 );

      new RefreshWhenVisible( this, model );
   }

   @Action
   public Task addConversation() throws ResourceException, IOException
   {
      final NameDialog dialog = topicDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( CaseResources.new_conversation_topic ) );

      if ( Strings.notEmpty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createConversation( dialog.name() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      model.notifyTransactions( transactions );

      if (Events.matches( Events.withNames("createdConversation" ), transactions ))
         list.setSelectedIndex( model.conversations().size() - 1 );
   }
}
