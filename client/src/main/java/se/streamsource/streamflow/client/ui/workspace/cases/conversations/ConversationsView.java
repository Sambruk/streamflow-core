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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.swing.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.workspace.cases.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.resource.conversation.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

import static se.streamsource.streamflow.client.util.i18n.*;

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

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task addConversation() throws ResourceException, IOException
   {
      final NameDialog dialog = topicDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( CaseResources.new_conversation_topic ) );

      if ( !Strings.empty( dialog.name() ) )
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

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      if (Events.matches( Events.withNames("createdConversation" ), transactions ))
         list.setSelectedIndex( model.conversations().size() - 1 );
   }
}
