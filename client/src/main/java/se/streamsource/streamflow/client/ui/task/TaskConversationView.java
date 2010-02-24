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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.streamflow.client.MacOsUIExtension;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class TaskConversationView extends JPanel implements ListEventListener
{

   private ObjectBuilderFactory obf;

   private TaskConversationModel model;
   private RefreshWhenVisible refresher;
   private JTextPane messages;
   private JTextPane newMessage;
   private JTextField topic;
   private ApplicationContext context;
   private JPanel bottomPanel;
   private TaskConversationParticipantsView participantView;
   private ActionMap am;
   private StateBinder newConversationBinder;
   private JTextField defaultFocusField;
   private JPanel sendPanel;

   public TaskConversationView(@Service ApplicationContext context,
         @Structure ObjectBuilderFactory obf)
   {
      super(new BorderLayout());
      this.context = context;
      this.obf = obf;
      am = context.getActionMap(this);
      MacOsUIExtension.convertAccelerators(am);

      messages = new JTextPane();
      messages.setContentType("text/html");
      ((HTMLEditorKit) messages.getEditorKit()).setAutoFormSubmission(false);
      messages.setEditable(false);

      JScrollPane scroll = new JScrollPane();
      scroll.getViewport().add(messages);
      add(scroll, BorderLayout.CENTER);

      add(initBottom(), BorderLayout.SOUTH);

      refresher = new RefreshWhenVisible(this);
      addAncestorListener(refresher);
   }

   private JPanel initBottom()
   {
      bottomPanel = new JPanel(new CardLayout());

      // INITIAL
      JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      createPanel.setPreferredSize(new Dimension(100, 40));
      // with a button for add message and add participant
      JButton newMsg = new JButton(am.get("showNewMessage"));
      createPanel.add(newMsg);

      // NEWMESSAGE
      sendPanel = new JPanel(new BorderLayout());
      sendPanel.setPreferredSize(new Dimension(100, 100));
      JScrollPane messageScroll = new JScrollPane();
      newMessage = new JTextPane();
      newMessage.setContentType("text/html");
      newMessage.setEditable(true);
      messageScroll.getViewport().add(newMessage);

      JPanel sendMessagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JButton addMessage = new JButton(am.get("addMessage"));
      JButton cancel = new JButton(am.get("cancel"));
      sendMessagePanel.add(addMessage);
      sendMessagePanel.add(cancel);

      sendPanel.add(messageScroll, BorderLayout.CENTER);
      sendPanel.add(sendMessagePanel, BorderLayout.SOUTH);

      bottomPanel.add(createPanel, "INITIAL");
      // bottomPanel.add( sendPanel, "NEW_MESSAGE" );

      ((CardLayout) bottomPanel.getLayout()).show(bottomPanel, "INITIAL");

      return bottomPanel;
   }

   @Action
   public void showNewMessage()
   {
      bottomPanel.add(sendPanel, "NEW_MESSAGE");
      ((CardLayout) bottomPanel.getLayout()).show(bottomPanel, "NEW_MESSAGE");
   }

   @Action
   public void addMessage()
   {
      model.addMessage(newMessage.getText());
      bottomPanel.remove(sendPanel);
      ((CardLayout) bottomPanel.getLayout()).show(bottomPanel, "INITIAL");
      newMessage.setText(null);
   }

   @Action
   public void cancel()
   {
      bottomPanel.remove(sendPanel);
      ((CardLayout) bottomPanel.getLayout()).show(bottomPanel, "INITIAL");
   }

   @Action
   public void showParticipants()
   {
      participantView.setModel(model.getParticipantsModel());
      ((CardLayout) bottomPanel.getLayout()).show(bottomPanel,
            "NEW_PARTICIPANT");
   }

   @Action
   public void participants()
   {
      // buf.append( "<strong>" + model.getDescription() + "</strong>" );
      // buf.append( "  ( " );
      // for(Object participant : model.participants() )
      // {
      // buf.append( ((LinkValue)participant).text() + " " );
      // }
      // buf.append( ")<br/>" );
      

   }

   public void setModel(TaskConversationModel taskConversationDetailModel)
   {
      if (model != null)
         model.messages().removeListEventListener(this);

      model = taskConversationDetailModel;
      model.refresh();
      refresher.setRefreshable(model);

      if (model != null)
      {
         taskConversationDetailModel.messages().addListEventListener(this);

         model.messages().addListEventListener(this);
         listChanged(null);
      }

   }

   public void listChanged(ListEvent listEvent)
   {
      EventList<MessageDTO> list = model.messages();
      StringBuffer buf = new StringBuffer();

      buf.append( "<html><head></head><body>");

      int size = list.size();
      if (size > 0)
      {
         buf.append("<table border='NONE' cellpadding='10'>");
         for (int i = 0; i < size; i++)
         {
            MessageDTO messageDTO = list.get(i);

            buf.append("<tr>");
            buf.append("<td width='150' align='left' valign='top'>");
            buf.append("<p>");
            buf.append(messageDTO.sender().get());
            buf.append("</p><p>");
            buf.append(new SimpleDateFormat(i18n
                  .text(WorkspaceResources.date_time_format)).format(messageDTO
                  .createdOn().get()));
            buf.append("</p></td><td width='" + getMessageTableLastColSize()
                  + "' style=''>");
            buf.append(messageDTO.text().get());
            buf
                  .append("<hr width='100%' style='border:1px solid #cccccc; padding-top: 15px;'>");
            buf.append("</td>");
            buf.append("</tr>");

         }
         buf.append("</table>");
      }
      buf.append( "</body></html>");
      messages.setText(buf.toString());
   }

   private int getMessageTableLastColSize()
   {
      return (int) (messages.getVisibleRect().getWidth() < 600 ? 450
            : (messages.getVisibleRect().getWidth() - 150));
   }

}
