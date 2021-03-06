/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.attachments;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.OpenAttachmentTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class AttachmentsView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private JXTable attachments;

   private EventJXTableModel<AttachmentDTO> tableModel;

   private AttachmentsModel attachmentsModel;

   public AttachmentsView(@Service ApplicationContext context, @Uses AttachmentsModel model)
   {
      setLayout(new BorderLayout());

      final ActionMap am = context.getActionMap(this);

      this.attachmentsModel = model;
      TableFormat tableFormat = new AttachmentsTableFormatter();
      tableModel = new EventJXTableModel<AttachmentDTO>(attachmentsModel.getEventList(), tableFormat);

      attachments = new JXTable(tableModel);

      attachments.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
      attachments.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

      attachments.getColumn( 0 ).setPreferredWidth( 20 );
      attachments.getColumn( 0 ).setMaxWidth( 20 );
      attachments.getColumn( 0 ).setResizable( false );
      attachments.getColumn(2).setPreferredWidth(100);
      attachments.getColumn(2).setMaxWidth(100);
      attachments.getColumn(3).setPreferredWidth(100);
      attachments.getColumn(3).setMaxWidth(100);

      attachments.setAutoCreateColumnsFromModel(false);

      attachments.addHighlighter(HighlighterFactory.createAlternateStriping());

      attachments.setModel(tableModel);
      attachments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      JPanel toolbar = new JPanel();
      StreamflowButton addButton = new StreamflowButton(am.get("add"));
      toolbar.add(addButton);
      StreamflowButton removeButton = new StreamflowButton(am.get("remove"));
      toolbar.add(removeButton);
      final StreamflowButton openButton = new StreamflowButton( am.get( "open" ) );
      toolbar.add( openButton );
      attachments.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "open" ) ) );
      attachments.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")){

         @Override
         public boolean isSelectedValueValid( javax.swing.Action action )
         {
            return !attachments.getValueAt( attachments.convertRowIndexToModel( attachments.getSelectedRow() ), 0 ).equals( Icons.formSubmitted );
         }
      });

      attachmentsModel.addObserver(new RefreshComponents().visibleOn("createattachment", addButton, removeButton));

      attachments.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
      attachments.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
      attachments.setActionMap(am);

      attachments.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent me)
         {
            int obj = attachments.getSelectedRow();
            if (obj == -1) return;
            if (me.getClickCount() == 2)
            {
               am.get("open").actionPerformed(new ActionEvent(openButton,
                     ActionEvent.ACTION_PERFORMED,
                     "open"));
               me.consume();
            }
         }
      });

      attachments.getColumn( 0 ).setCellRenderer( new DefaultTableCellRenderer()
      {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
         {

            JLabel iconLabel = new JLabel( " " );
            switch ( (Icons)value )
            {
               case attachments:
                  iconLabel = new JLabel( i18n.icon( (Icons)value, 11 ) );
                  break;

               case conversations:
                  iconLabel = new JLabel( i18n.icon( (Icons)value, 11 ) );
                  break;

               case formSubmitted:
                  iconLabel = new JLabel( i18n.icon( (Icons)value, 11 ) );
                  break;
            }

            iconLabel.setOpaque( true );

            if (isSelected)
               iconLabel.setBackground( attachments.getSelectionBackground() );
            return iconLabel;
         }
      } );

      JScrollPane attachmentsScrollPane = new JScrollPane(attachments);

      add(attachmentsScrollPane, BorderLayout.CENTER);
      add(toolbar, BorderLayout.SOUTH);

      new RefreshWhenShowing(this, attachmentsModel);
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task add() throws IOException
   {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled(true);

      if (fileChooser.showDialog(this, i18n.text(WorkspaceResources.create_attachment)) == JFileChooser.APPROVE_OPTION)
      {
         final File[] selectedFiles = fileChooser.getSelectedFiles();

         return new AddAttachmentTask(selectedFiles);
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage(i18n.text(attachments.getSelectedRows().length > 1
            ? WorkspaceResources.attachments
            : WorkspaceResources.attachment));
      dialogs.showOkCancelHelpDialog(this, dialog, i18n.text(StreamflowResources.confirmation));

      if (dialog.isConfirmed())
      {
         final List<AttachmentDTO> removedAttachments = new ArrayList<AttachmentDTO>();
         for (int i : attachments.getSelectedRows())
         {
            removedAttachments.add(attachmentsModel.getEventList().get(attachments.convertRowIndexToModel(i)));
         }

         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {

          for (AttachmentDTO removedAttachment : removedAttachments)
          {
             attachmentsModel.removeAttachment(removedAttachment);
          }

            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task open() throws IOException
   {
      for (int i : attachments.getSelectedRows())
      {
         AttachmentDTO attachment = attachmentsModel.getEventList().get(attachments.convertRowIndexToModel(i));

         return new OpenAttachmentTask( attachment.text().get(), attachment.href().get(), this, attachmentsModel, dialogs );
      }

      return null;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      // on usecase delete no update necessary
      if( matches( withUsecases( "delete" ), transactions ))
      {
         if( matches(  withNames( "removedAttachment" ), transactions ))
            attachmentsModel.refresh();
         else
            return;
      }

      else if ( matches( withNames( "changedStatus", "addedAttachment" ), transactions ))
         attachmentsModel.refresh();
   }

   private class AddAttachmentTask
         extends CommandTask
   {
      File[] selectedFiles;

      private AddAttachmentTask(File[] selectedFiles)
      {
         this.selectedFiles = selectedFiles;
      }

      @Override
      public void command()
            throws Exception
      {
         setTitle(getResourceMap().getString("title"));
         String message = getResourceMap().getString( "message" );
         for (File file : selectedFiles)
         {
            setMessage( message + " " + file.getName() );
            FileInputStream fin = new FileInputStream(file);
            attachmentsModel.createAttachment(file, fin);
         }
      }
   }
}
