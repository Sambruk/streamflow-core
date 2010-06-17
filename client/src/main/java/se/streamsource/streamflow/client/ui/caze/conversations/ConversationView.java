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

package se.streamsource.streamflow.client.ui.caze.conversations;

import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.caze.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.GroupedFilterListDialog;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class ConversationView extends JPanel
{
   @Uses
   ObjectBuilder<GroupedFilterListDialog> participantsDialog;

   @Service
   DialogService dialogs;

   private ObjectBuilderFactory obf;

   private ConversationModel model;
   private MessagesView messagesView;
   private JTextPane newMessage;
   private ApplicationContext context;
   private JPanel bottomPanel;
   private JPanel topPanel;
   private JButton addParticipants;
   private ConversationParticipantsView participantsView;
   private JPanel sendPanel;
   private JPanel participantsButtonPanel;

   public ConversationView( @Service final ApplicationContext context,
                                @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.context = context;
      this.obf = obf;

      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( getActionMap() );

      add( initTop(), BorderLayout.NORTH );

      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));
      messagesView = obf.newObjectBuilder( MessagesView.class ).newInstance();
      messagesView.setContentType( "text/html" );
      ((HTMLEditorKit) messagesView.getEditorKit()).setAutoFormSubmission( false );
      messagesView.setEditable( false );

      JScrollPane scroll = new JScrollPane();
      scroll.getViewport().add( messagesView );
      add( scroll, BorderLayout.CENTER );

      add( initBottom(), BorderLayout.SOUTH );

   }

   private JPanel initTop()
   {
      topPanel = new JPanel( new BorderLayout() );

      participantsView = obf.newObjectBuilder( ConversationParticipantsView.class ).newInstance();
      participantsView.setPreferredSize( new Dimension( 800, 50 ) );
      
      javax.swing.Action addParticipantsAction = getActionMap().get(
            "addParticipants" );
      addParticipants = new JButton( addParticipantsAction );
      addParticipants.registerKeyboardAction( addParticipantsAction,
            (KeyStroke) addParticipantsAction
                  .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // IMPLODED
      participantsButtonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

      participantsButtonPanel.add( addParticipants );

      topPanel.add(participantsView, BorderLayout.CENTER );
      topPanel.add( participantsButtonPanel, BorderLayout.EAST );
      return topPanel;
   }

   private JPanel initBottom()
   {
      bottomPanel = new JPanel( new CardLayout() );

      // INITIAL
      JPanel createPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
      javax.swing.Action writeMessageAction = getActionMap()
            .get( "writeMessage" );
      JButton writeMessage = new JButton( writeMessageAction );
      writeMessage.registerKeyboardAction( writeMessageAction,
            (KeyStroke) writeMessageAction
                  .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      createPanel.add( writeMessage );

      // NEWMESSAGE
      sendPanel = new JPanel( new BorderLayout() );
      sendPanel.setPreferredSize( new Dimension( 100, 200 ) );
      JScrollPane messageScroll = new JScrollPane();
      newMessage = new JTextPane();
      newMessage.setContentType( "text/html" );
      newMessage.setEditable( true );
      messageScroll.getViewport().add( newMessage );

      JPanel sendMessagePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
      javax.swing.Action sendMessageAction = getActionMap().get( "sendMessage" );
      JButton sendMessage = new JButton( sendMessageAction );
      sendMessage.registerKeyboardAction( sendMessageAction,
            (KeyStroke) sendMessageAction
                  .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      javax.swing.Action cancelAction = getActionMap().get( "cancelNewMessage" );
      JButton cancel = new JButton( cancelAction );

      sendMessagePanel.add( sendMessage );
      sendMessagePanel.add( cancel );

      sendPanel.add( messageScroll, BorderLayout.CENTER );
      sendPanel.add( sendMessagePanel, BorderLayout.SOUTH );

      bottomPanel.add( createPanel, "INITIAL" );

      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "INITIAL" );

      return bottomPanel;
   }

   @Action
   public void writeMessage()
   {
      bottomPanel.add( sendPanel, "NEW_MESSAGE" );
      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "NEW_MESSAGE" );

      newMessage.requestFocusInWindow();
   }

   @Action
   public void sendMessage()
   {
      model.getMessagesModel().addMessage( newMessage.getText() );
      bottomPanel.remove( sendPanel );
      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "INITIAL" );
      newMessage.setText( null );
   }

   @Action
   public void cancelNewMessage()
   {
      bottomPanel.remove( sendPanel );
      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "INITIAL" );
      newMessage.setText( null );
   }

   @Action
   public void addParticipants()
   {
      GroupedFilterListDialog dialog = participantsDialog.use(
            i18n.text( CaseResources.choose_participant ),
            model.getParticipantsModel().possibleParticipants() ).newInstance();
      dialogs.showOkCancelHelpDialog( addParticipants, dialog );

      for (EntityReference entityReference : dialog.getSelectedReferences())
      {
         model.getParticipantsModel().addParticipant( entityReference );
      }
   }

   public void setModel( ConversationModel conversationModel )
   {
      model = conversationModel;
      model.refresh();
      participantsView.setModel( model.getParticipantsModel() );
      messagesView.setModel( model.getDescription(), model.getMessagesModel() );
   }
}
