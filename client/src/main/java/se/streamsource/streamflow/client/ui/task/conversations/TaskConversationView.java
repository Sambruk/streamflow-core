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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.FilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

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

public class TaskConversationView extends JPanel
{
   @Uses
   ObjectBuilder<FilterListDialog> participantsDialog;

   @Service
   DialogService dialogs;

   private ObjectBuilderFactory obf;

   private TaskConversationModel model;
   private MessagesView messagesView;
   private JTextPane newMessage;
   private ApplicationContext context;
   private JPanel bottomPanel;
   private JPanel topPanel;
   private JButton addParticipants;
   private TaskConversationParticipantsView participantsView;
   private JPanel sendPanel;
   private JPanel participantsButtonPanel;

   public TaskConversationView( @Service final ApplicationContext context,
                                @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.context = context;
      this.obf = obf;

      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( getActionMap() );

      add( initTop(), BorderLayout.NORTH );

      messagesView = obf.newObjectBuilder( MessagesView.class ).newInstance();
      messagesView.setContentType( "text/html" );
      ((HTMLEditorKit) messagesView.getEditorKit()).setAutoFormSubmission( false );
      messagesView.setEditable( false );

      JScrollPane scroll = new JScrollPane();
      scroll.getViewport().add( messagesView );
      add( scroll, BorderLayout.CENTER );

      add( initBottom(), BorderLayout.SOUTH );

      // refresher = new RefreshWhenVisible(this);
      // addAncestorListener(refresher);
   }

   private JPanel initTop()
   {
      topPanel = new JPanel( new BorderLayout() );

      // javax.swing.Action allParticipantsAction = getActionMap().get(
      // "allParticipants");
      // JButton allParticipants = new JButton(allParticipantsAction);
      // allParticipants.registerKeyboardAction(allParticipantsAction,
      // (KeyStroke) allParticipantsAction
      // .getValue(javax.swing.Action.ACCELERATOR_KEY),
      // JComponent.WHEN_IN_FOCUSED_WINDOW);


      participantsView = obf.newObjectBuilder( TaskConversationParticipantsView.class ).newInstance();

      javax.swing.Action addParticipantsAction = getActionMap().get(
            "addParticipants" );
      addParticipants = new JButton( addParticipantsAction );
      addParticipants.registerKeyboardAction( addParticipantsAction,
            (KeyStroke) addParticipantsAction
                  .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // IMPLODED
      participantsButtonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
      // participantsButtonPanel.add(allParticipants);
      participantsButtonPanel.add( participantsView );
      participantsButtonPanel.add( addParticipants );

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
      sendPanel.setPreferredSize( new Dimension( 100, 100 ) );
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

   // @Action
   // public void allParticipant's()
   // {
   // }

   @Action
   public void addParticipants()
   {
      FilterListDialog dialog = participantsDialog.use(
            i18n.text( WorkspaceResources.choose_participant ),
            model.getParticipantsModel().possibleParticipants() ).newInstance();
      dialogs.showOkCancelHelpDialog( addParticipants, dialog );

      if(dialog.getSelected() != null )
      {
         model.getParticipantsModel().addParticipant( dialog.getSelected() );
      }
   }

   public void setModel( TaskConversationModel taskConversationModel )
   {
      model = taskConversationModel;
      model.refresh();
      participantsView.setModel( model.getParticipantsModel() );
      messagesView.setModel( model.getDescription(), model.getMessagesModel() );
   }
}
