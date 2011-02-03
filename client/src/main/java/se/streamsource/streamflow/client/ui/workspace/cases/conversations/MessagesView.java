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
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.resource.conversation.MessageDTO;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class MessagesView extends JPanel
      implements ListEventListener<MessageDTO>, TransactionListener
{
   private MessagesModel model;

   private int lastSize = -1;
   private Box messages;
   private JPanel writeMessagePanel;
   private JPanel sendPanel;
   private JTextPane newMessage;

   private Color[] messageColors = new Color[]{Color.lightGray.brighter(), new Color(238,244,253)};

   public MessagesView(@Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( getActionMap() );

      setLayout(new BorderLayout());

      model = obf.newObjectBuilder( MessagesModel.class ).use(client).newInstance();

      model.messages().addListEventListener( this );

      messages = Box.createVerticalBox();

      new RefreshWhenShowing(this, model);

      initWriteMessage();

      JScrollPane scrollMessages = new JScrollPane(messages);

      add(scrollMessages, BorderLayout.CENTER);
      add(writeMessagePanel, BorderLayout.SOUTH );
   }

   public void listChanged( ListEvent<MessageDTO> listEvent )
   {
      EventList<MessageDTO> list = model.messages();

      if (list.size() > lastSize)
      {
         messages.removeAll();

         int size = list.size();
         if (size > 0)
         {
            SimpleDateFormat dateFormat = new SimpleDateFormat( i18n
                  .text( WorkspaceResources.date_time_format ) );
            int idx = 0;
            for (MessageDTO messageDTO : list)
            {
               JLabel message = new JLabel( "<html>"+messageDTO.sender().get() + ", " + dateFormat.format( messageDTO.createdOn().get() )+":"+messageDTO.text().get().replace("\n","<br>" )+"</html>" );
               message.setBackground( messageColors[idx%2] );
               message.setOpaque( true );
               idx++;
/*
               Box message = Box.createHorizontalBox();
               message.setBorder( BorderFactory.createTitledBorder( ) );
               message.add();
               message.add(Box.createHorizontalGlue());
*/

               messages.add( message );
/*
               buf.append( "<tr>" );
               buf.append( "<td width='150' align='left' valign='top'>" );
               buf.append( "<p>" );
               buf.append( messageDTO.sender().get() );
               buf.append( "</p><p>" );
               buf.append( new SimpleDateFormat( i18n
                     .text( WorkspaceResources.date_time_format ) ).format( messageDTO
                     .createdOn().get() ) );
               buf.append( "</p></td><td width='" ).append( getMessageTableLastColSize() ).append( "' style=''>" );
               buf.append( messageDTO.text().get().replace("\n","<br>" ));
               buf
                     .append( "<hr width='100%' style='border:1px solid #cccccc; padding-top: 15px;'>" );
               buf.append( "</td>" );
               buf.append( "</tr>" );
*/

            }
//            buf.append( "</table>" );
         }
//         buf.append( "</body></html>" );
         messages.revalidate();
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
      newMessage.setContentType( "text/plain" );
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

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames("createdMessage"), transactions ))
      {
         model.refresh();
      }
   }
}