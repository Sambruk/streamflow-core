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

package se.streamsource.streamflow.client.ui.workspace.cases.attachments;

import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.object.*;
import org.restlet.representation.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.domain.attachment.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * JAVADOC
 */
public class AttachmentsView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private JXTable attachments;

   private EventJXTableModel<AttachmentValue> tableModel;

   private AttachmentsModel attachmentsModel;

   public AttachmentsView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      setLayout( new BorderLayout() );

      final ActionMap am = context.getActionMap( this );

      this.attachmentsModel = obf.newObjectBuilder( AttachmentsModel.class ).use( client ).newInstance();
      TableFormat tableFormat = new AttachmentsTableFormatter();
      tableModel = new EventJXTableModel<AttachmentValue>( attachmentsModel.getEventList(), tableFormat );

      attachments = new JXTable(tableModel);

      attachments.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      attachments.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      attachments.getColumn( 1 ).setPreferredWidth( 100 );
      attachments.getColumn( 1 ).setMaxWidth( 100 );
      attachments.getColumn( 2 ).setPreferredWidth( 100 );
      attachments.getColumn( 2 ).setMaxWidth( 100 );

      attachments.setAutoCreateColumnsFromModel( false );

      attachments.addHighlighter( HighlighterFactory.createAlternateStriping() );

      attachments.setModel( tableModel );
      attachments.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      JPanel toolbar = new JPanel();
      JButton addButton = new JButton( am.get( "add" ) );
      toolbar.add( addButton );
      JButton removeButton = new JButton( am.get( "remove" ) );
      toolbar.add( removeButton );
      toolbar.add( new JButton( am.get( "open" ) ) );
      attachments.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "open" ) ) );
      attachmentsModel.addObserver(new RefreshComponents().visibleOn( "createattachment", addButton, removeButton ));

      attachments.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "open" );
      attachments.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), "remove" );
      attachments.setActionMap( am );

      attachments.addMouseListener( new MouseAdapter()
      {
         public void mouseClicked( MouseEvent me )
         {
            int obj = attachments.getSelectedRow();
            if (obj == -1) return;
            if (me.getClickCount() == 2)
            {
               am.get( "open" ).actionPerformed( new ActionEvent( this,
                     ActionEvent.ACTION_PERFORMED,
                     "open" ) );
               me.consume();
            }
         }
      } );

      JScrollPane attachmentsScrollPane = new JScrollPane( attachments );

      add( attachmentsScrollPane, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenShowing( this, attachmentsModel );
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task add() throws IOException
   {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled( true );

      if (fileChooser.showDialog( this, i18n.text( WorkspaceResources.create_attachment ) ) == JFileChooser.APPROVE_OPTION)
      {
         final File[] selectedFiles = fileChooser.getSelectedFiles();

         return new AddAttachmentTask(selectedFiles);
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( i18n.text( attachments.getSelectedRows().length > 1
            ? WorkspaceResources.attachments
            : WorkspaceResources.attachment ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );

      if (dialog.isConfirmed())
      {
         final List<AttachmentValue> removedAttachments = new ArrayList<AttachmentValue>( );
         for (int i : attachments.getSelectedRows())
         {
            removedAttachments.add(attachmentsModel.getEventList().get( attachments.convertRowIndexToModel( i ) ));
         }

         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               try
               {
                  for (AttachmentValue removedAttachment : removedAttachments)
                  {
                     attachmentsModel.removeAttachment( removedAttachment );
                  }
               } catch (Throwable e)
               {
                  e.printStackTrace();
               }
            }
         };
      } else
         return null;
   }

   @Action
   public Task open() throws IOException
   {
      for (int i : attachments.getSelectedRows())
      {
         AttachmentValue attachment = attachmentsModel.getEventList().get( attachments.convertRowIndexToModel( i ) );

         return new OpenAttachmentTask( attachment );
      }

      return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames("changedStatus", "addedAttachment", "removedAttachment" ), transactions ))
         attachmentsModel.refresh();
   }

   private class AddAttachmentTask
      extends CommandTask
   {
      File[] selectedFiles;

      private AddAttachmentTask( File[] selectedFiles )
      {
         this.selectedFiles = selectedFiles;
      }

      @Override
      public void command()
         throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         for (File file : selectedFiles)
         {
            FileInputStream fin = new FileInputStream( file );
            attachmentsModel.createAttachment( file, fin );
         }
      }
   }

   private class OpenAttachmentTask extends Task<File, Void>
   {
      private final AttachmentValue attachment;

      public OpenAttachmentTask( AttachmentValue attachment )
      {
         super( Application.getInstance() );
         this.attachment = attachment;

         setUserCanCancel( false );
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         String fileName = attachment.text().get();
         String[] fileNameParts = fileName.split( "\\." );
         Representation representation = attachmentsModel.download( attachment );

         File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );

         Inputs.byteBuffer( representation.getStream(), 8192 ).transferTo( Outputs.byteBuffer( file ));

         return file;
      }

      @Override
      protected void succeeded( File file )
      {
         // Open file
         Desktop desktop = Desktop.getDesktop();
         try
         {
            desktop.edit( file );
         } catch (IOException e)
         {
            try
            {
               desktop.open( file );
            } catch (IOException e1)
            {
               dialogs.showMessageDialog( AttachmentsView.this, i18n.text( WorkspaceResources.could_not_open_attachment ), "" );
            }
         }
      }
   }
}
