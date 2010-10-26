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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.resource.conversation.MessageDTO;

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
import java.text.SimpleDateFormat;

public class MessagesView extends JPanel
      implements ListEventListener<MessageDTO>, TransactionListener
{
   private MessagesModel model;

   private int lastSize = -1;
   private JTextPane messages;
   private JPanel writeMessagePanel;
   private JPanel sendPanel;
   private JTextPane newMessage;

   public MessagesView(@Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( getActionMap() );

      setLayout(new BorderLayout());

      model = obf.newObjectBuilder( MessagesModel.class ).use(client).newInstance();

      model.messages().addListEventListener( this );

      messages = new JTextPane();
      messages.setContentType( "text/html" );
      ((HTMLEditorKit) messages.getEditorKit()).setAutoFormSubmission( false );
      messages.setEditable( false );

      new RefreshWhenVisible(this, model);

      initWriteMessage();

      JScrollPane scrollMessages = new JScrollPane();
      scrollMessages.getViewport().add( messages );

      add(scrollMessages, BorderLayout.CENTER);
      add(writeMessagePanel, BorderLayout.SOUTH );
   }

   public void listChanged( ListEvent<MessageDTO> listEvent )
   {
      EventList<MessageDTO> list = model.messages();

      if (list.size() > lastSize)
      {
         StringBuffer buf = new StringBuffer();

         buf.append( "<html><head></head><body>" );

         int size = list.size();
         if (size > 0)
         {
            buf.append( "<table border='NONE' cellpadding='10'>" );
            for (int i = 0; i < size; i++)
            {
               MessageDTO messageDTO = list.get( i );

               buf.append( "<tr>" );
               buf.append( "<td width='150' align='left' valign='top'>" );
               buf.append( "<p>" );
               buf.append( messageDTO.sender().get() );
               buf.append( "</p><p>" );
               buf.append( new SimpleDateFormat( i18n
                     .text( WorkspaceResources.date_time_format ) ).format( messageDTO
                     .createdOn().get() ) );
               buf.append( "</p></td><td width='" ).append( getMessageTableLastColSize() ).append( "' style=''>" );
               buf.append( messageDTO.text().get() );
               buf
                     .append( "<hr width='100%' style='border:1px solid #cccccc; padding-top: 15px;'>" );
               buf.append( "</td>" );
               buf.append( "</tr>" );

            }
            buf.append( "</table>" );
         }
         buf.append( "</body></html>" );
         messages.setText( buf.toString() );
         lastSize = list.size();
      }
   }


   private void initWriteMessage()
   {
      writeMessagePanel = new JPanel( new CardLayout() );

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

      writeMessagePanel.add( createPanel, "INITIAL" );

      ((CardLayout) writeMessagePanel.getLayout()).show( writeMessagePanel, "INITIAL" );
   }

   @Action
   public void writeMessage()
   {
      writeMessagePanel.add( sendPanel, "NEW_MESSAGE" );
      ((CardLayout) writeMessagePanel.getLayout()).show( writeMessagePanel, "NEW_MESSAGE" );

      newMessage.requestFocusInWindow();
   }

   @Action
   public Task sendMessage()
   {
      final String messageText = newMessage.getText();
      writeMessagePanel.remove( sendPanel );
      ((CardLayout) writeMessagePanel.getLayout()).show( writeMessagePanel, "INITIAL" );
      newMessage.setText( null );
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.createMessage( messageText );
         }
      };
   }

   @Action
   public void cancelNewMessage()
   {
      writeMessagePanel.remove( sendPanel );
      ((CardLayout) writeMessagePanel.getLayout()).show( writeMessagePanel, "INITIAL" );
      newMessage.setText( null );
   }

   private int getMessageTableLastColSize()
   {
      return (int) (getVisibleRect().getWidth() < 600 ? 450
            : (getVisibleRect().getWidth() - 150));
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames("createdMessage"), transactions ))
      {
         model.refresh();
      }
   }
}