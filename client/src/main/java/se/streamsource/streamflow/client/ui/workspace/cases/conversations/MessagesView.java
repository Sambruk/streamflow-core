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

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.created_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.message_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.sender_column_header;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class MessagesView extends JPanel implements TransactionListener
{
   private static final long serialVersionUID = -4508473068931275932L;

   private MessagesModel model;

   private JXTable messageTable;
   private JPanel detailMessagePanel;
   private JPanel sendPanel;
   private JPanel showPanel;
   private JTextPane newMessage;
   private JTextPane showMessage;
   private JXLabel authorLabelValue;
   private JXLabel createdOnLabelValue;

   public MessagesView(@Service ApplicationContext context, @Uses MessagesModel model)
   {
      setActionMap(context.getActionMap(this));
      MacOsUIWrapper.convertAccelerators(getActionMap());

      setLayout(new BorderLayout());

      this.model = model;

      messageTable = new JXTable(new EventJXTableModel<MessageDTO>(model.messages(), new TableFormat<MessageDTO>()
      {
         String[] columnNames = new String[]
         { text(sender_column_header), text(message_column_header), text(created_column_header) };

         public int getColumnCount()
         {
            return columnNames.length;
         }

         public String getColumnName(int i)
         {
            return columnNames[i];
         }

         public Object getColumnValue(MessageDTO o, int i)
         {
            switch (i)
            {
            case 0:
               return o.sender().get();
            case 1:
               return o.text().get();
            case 2:
               return o.createdOn().get();
            }

            return null;
         }
      }));

      messageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      messageTable
            .setFocusTraversalKeys(
                  KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                  KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
                        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
      messageTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

      messageTable.getColumn(2).setCellRenderer(new DefaultTableRenderer(new StringValue()
      {
         private static final long serialVersionUID = -6677363096055906298L;

         public String getString(Object value)
         {
            return DateFormats.getProgressiveDateTimeValue((Date) value, Locale.getDefault());
         }
      }));

      ListSelectionModel selectionModel = messageTable.getSelectionModel();
      selectionModel.addListSelectionListener(new MessageListSelectionHandler());

      messageTable.addHighlighter(HighlighterFactory.createAlternateStriping());

      messageTable.getColumn(0).setPreferredWidth(100);
      messageTable.getColumn(1).setPreferredWidth(300);
      messageTable.getColumn(2).setPreferredWidth(60);
      messageTable.getColumn(2).setMaxWidth(100);

      initDetailMessage();

      JScrollPane scrollMessages = new JScrollPane(messageTable);

      add(scrollMessages, BorderLayout.CENTER);
      add(detailMessagePanel, BorderLayout.SOUTH);

      new RefreshWhenShowing(this, model);
   }
   

   private void initDetailMessage()
   {
      detailMessagePanel = new JPanel(new CardLayout());

      // INITIAL
      JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      javax.swing.Action writeMessageAction = getActionMap().get("writeMessage");
      StreamflowButton writeMessage = new StreamflowButton(writeMessageAction);
      writeMessage.registerKeyboardAction(writeMessageAction,
            (KeyStroke) writeMessageAction.getValue(javax.swing.Action.ACCELERATOR_KEY),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      createPanel.add(writeMessage);

      // NEWMESSAGE
      sendPanel = new JPanel(new BorderLayout());
      sendPanel.setPreferredSize(new Dimension(100, 250));
      JScrollPane messageScroll = new JScrollPane();

      newMessage = new JTextPane();
      newMessage.setContentType("text/plain");
      newMessage.setEditable(true);
      messageScroll.getViewport().add(newMessage);

      JPanel sendMessagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      javax.swing.Action sendMessageAction = getActionMap().get("sendMessage");
      StreamflowButton sendMessage = new StreamflowButton(sendMessageAction);
      sendMessage.registerKeyboardAction(sendMessageAction,
            (KeyStroke) sendMessageAction.getValue(javax.swing.Action.ACCELERATOR_KEY),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      javax.swing.Action cancelAction = getActionMap().get("cancelNewMessage");
      StreamflowButton cancel = new StreamflowButton(cancelAction);

      sendMessagePanel.add(sendMessage);
      sendMessagePanel.add(cancel);

      sendPanel.add(messageScroll, BorderLayout.CENTER);
      sendPanel.add(sendMessagePanel, BorderLayout.SOUTH);

      // SHOWMESSAGE
      showPanel = new JPanel(new BorderLayout());
      showPanel.setPreferredSize(new Dimension(100, 250));
      showPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
      JScrollPane messageShowScroll = new JScrollPane();

      JPanel messageDetailButtonPanel = new JPanel(new BorderLayout());
      messageDetailButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));
      javax.swing.Action closeAction = getActionMap().get("closeMessageDetails");
      StreamflowButton closeButton = new StreamflowButton(closeAction);
      JPanel closeButtonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      closeButtonPanel.setBorder( BorderFactory.createEmptyBorder( 7, 0, 0, 0 ) );
      closeButtonPanel.add( closeButton );
      
      FormLayout detailHeaderLayout = new FormLayout("35dlu, 2dlu, pref:grow", "pref, pref");
      JPanel messageDetailsLabelPanel = new JPanel();
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(detailHeaderLayout, messageDetailsLabelPanel);
      
      JXLabel authorLabel = new JXLabel(text(sender_column_header));
      JXLabel createdOnLabel = new JXLabel(text(created_column_header));
      authorLabel.setForeground(Color.GRAY);
      createdOnLabel.setForeground(Color.GRAY);
      
      authorLabelValue = new JXLabel();
      createdOnLabelValue = new JXLabel();
      
      formBuilder.setExtent(1, 1);
      formBuilder.add(authorLabel);
      formBuilder.nextColumn(2);
      formBuilder.add(authorLabelValue);
      formBuilder.nextLine();
      formBuilder.add(createdOnLabel);
      formBuilder.nextColumn(2);
      formBuilder.add(createdOnLabelValue);
      
      messageDetailButtonPanel.add(closeButtonPanel, BorderLayout.EAST);
      messageDetailButtonPanel.add(messageDetailsLabelPanel, BorderLayout.WEST);

      showMessage = new JTextPane();
      showMessage.setContentType("text/plain");
      showMessage.setEditable(false);
      messageShowScroll.getViewport().add(showMessage);

      StyledDocument doc = showMessage.getStyledDocument();
      Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

      Style regular = doc.addStyle("regular", def);
      StyleConstants.setFontFamily(def, "SansSerif");

      Style s = doc.addStyle("italic", regular);
      StyleConstants.setItalic(s, true);

      s = doc.addStyle("bold", regular);
      StyleConstants.setBold(s, true);

      showPanel.add(messageShowScroll, BorderLayout.CENTER);
      showPanel.add(messageDetailButtonPanel, BorderLayout.NORTH);

      detailMessagePanel.add(createPanel, "INITIAL");

      ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "INITIAL");
   }

   @Action
   public void writeMessage()
   {
      detailMessagePanel.add(sendPanel, "NEW_MESSAGE");
      ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "NEW_MESSAGE");

      newMessage.requestFocusInWindow();
   }

   @Action
   public Task sendMessage()
   {
      final String messageText = newMessage.getText();
      detailMessagePanel.remove(sendPanel);
      ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "INITIAL");
      newMessage.setText(null);
      return new CommandTask()
      {
         @Override
         public void command() throws Exception
         {
            model.createMessage(messageText);
         }
      };
   }

   @Action
   public void cancelNewMessage()
   {
      detailMessagePanel.remove(sendPanel);
      ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "INITIAL");
      newMessage.setText(null);
   }

   @Action
   public void closeMessageDetails()
   {
      messageTable.getSelectionModel().clearSelection();
      detailMessagePanel.remove(showPanel);
      ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "INITIAL");
      showMessage.setText(null);
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches(Events.withNames("createdMessage"), transactions))
      {
         model.refresh();
      }
   }

   class MessageListSelectionHandler implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {

         if (!e.getValueIsAdjusting())
         {
            int index = messageTable.getSelectedRow();
            if (index >= 0)
            {
               showMessage.setText(null);
               StyledDocument doc = showMessage.getStyledDocument();
               try
               {
                  authorLabelValue.setText(model.messages().get(index).sender().get());
                  createdOnLabelValue.setText(DateFormats.getFullDateTimeValue(
                        model.messages().get(index).createdOn().get(), Locale.getDefault()));

                  doc.insertString(0, model.messages().get(index).text().get(), doc.getStyle("regular"));
               } catch (BadLocationException e1)
               {
                  e1.printStackTrace();
               }

               detailMessagePanel.add(showPanel, "SHOW_MESSAGE");
               ((CardLayout) detailMessagePanel.getLayout()).show(detailMessagePanel, "SHOW_MESSAGE");
            }
         }
      }
   }
}