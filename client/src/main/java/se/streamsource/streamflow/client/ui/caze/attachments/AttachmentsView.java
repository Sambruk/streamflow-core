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

package se.streamsource.streamflow.client.ui.caze.attachments;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.engine.io.BioUtils;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

/**
 * JAVADOC
 */
public class AttachmentsView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private JXTable attachments = new JXTable();

   private EventJXTableModel<AttachmentValue> tableModel;

   private AttachmentsModel attachmentsModel;
   public RefreshWhenVisible refresher;

   //public void init( @Service ApplicationContext context )
   public AttachmentsView( @Service ApplicationContext context )
   {
      setLayout( new BorderLayout() );

      final ActionMap am = context.getActionMap( this );

      TableFormat tableFormat = new AttachmentsTableFormatter();

      tableModel = new EventJXTableModel<AttachmentValue>( new BasicEventList<AttachmentValue>(), tableFormat );

      attachments.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS ) );
      attachments.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS ) );

      TableColumn nameColumn = new TableColumn( 0 );
      nameColumn.setHeaderValue( tableFormat.getColumnName( 0 ) );
      TableColumn sizeColumn = new TableColumn( 1 );
      sizeColumn.setHeaderValue( tableFormat.getColumnName( 1 ) );
      TableColumn dateColumn = new TableColumn( 2 );
      dateColumn.setHeaderValue( tableFormat.getColumnName( 2 ) );

      attachments.getColumnModel().addColumn( nameColumn );
      attachments.getColumnModel().addColumn( sizeColumn );
      attachments.getColumnModel().addColumn( dateColumn );

      attachments.getColumn( 1 ).setPreferredWidth( 100 );
      attachments.getColumn( 1 ).setMaxWidth( 100 );
      attachments.getColumn( 2 ).setPreferredWidth( 100 );
      attachments.getColumn( 2 ).setMaxWidth( 100 );

      attachments.setAutoCreateColumnsFromModel( false );

      attachments.addHighlighter( HighlighterFactory.createAlternateStriping() );

      attachments.setModel( tableModel );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      toolbar.add( new JButton( am.get( "open" ) ) );
      attachments.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ), am.get( "open" ) ) );

      attachments.getInputMap( ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "open" );
      attachments.getInputMap(  ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), "remove" );
      attachments.setActionMap( am );
      
      attachments.addMouseListener( new MouseAdapter()
      {
         public void mouseClicked( MouseEvent me )
         {
            int obj = attachments.getSelectedRow();
            if (obj == -1) return;
            if (me.getClickCount() == 2)
            {
               am.get("open").actionPerformed( new ActionEvent( this,
                     ActionEvent.ACTION_PERFORMED,
                     "open" ) );
               me.consume();
            }
         }
      } );
      
      JScrollPane attachmentsScrollPane = new JScrollPane( attachments );

      add( attachmentsScrollPane, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );
      
      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   @Action
   public void add() throws IOException
   {
      JFileChooser fileChooser = new JFileChooser();

      if (fileChooser.showDialog( this, i18n.text( WorkspaceResources.create_attachment ) ) == JFileChooser.APPROVE_OPTION)
      {
         // Progress bar for upload
         JProgressBar progressBar;

         File selectedFile = fileChooser.getSelectedFile();

         FileInputStream fin = new FileInputStream(selectedFile);
         ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(this, i18n.text(WorkspaceResources.uploading_file), fin);

         attachmentsModel.createAttachment(selectedFile, pmis);
         attachmentsModel.refresh();
      }
   }

   @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( i18n.text( attachments.getSelectedRows().length > 1
            ? WorkspaceResources.attachments
            : WorkspaceResources.attachment ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );

      if (dialog.isConfirmed())
      {
         for (int i : attachments.getSelectedRows())
         {
            AttachmentValue attachment = attachmentsModel.getEventList().get( attachments.convertRowIndexToModel( i ) );
            attachmentsModel.removeAttachment( attachment );
         }
         attachmentsModel.refresh();
      }
   }

   @Action
   public void open() throws IOException
   {
      for (int i : attachments.getSelectedRows())
      {
         AttachmentValue attachment = attachmentsModel.getEventList().get( attachments.convertRowIndexToModel( i ) );
         String fileName = attachment.text().get();
         String[] fileNameParts = fileName.split( "\\." );
         File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );
         FileOutputStream out = new FileOutputStream( file );

         InputStream in = attachmentsModel.download( attachment );
         try
         {
            BioUtils.copy( in, out );
         } catch (IOException e)
         {
            in.close();
            out.close();
            throw e;
         } finally
         {
            try
            {
               in.close();
               out.close();
            } catch (IOException e)
            {
               // Ignore
            }
         }

         // Open file
         try
         {
            Desktop.getDesktop().edit( file );
         } catch (IOException e)
         {
            try
            {
               Desktop.getDesktop().open( file );
            } catch (IOException e1)
            {
               dialogs.showOkDialog( this, new JLabel(i18n.text( WorkspaceResources.could_not_open_attachment)) );
            }
         }
      }
   }

   public void setModel( AttachmentsModel attachmentsModel )
   {
      this.attachmentsModel = attachmentsModel;
      tableModel = new EventJXTableModel<AttachmentValue>( attachmentsModel.getEventList(), new AttachmentsTableFormatter() );
      attachments.setModel( tableModel );
      refresher.setRefreshable( attachmentsModel );
   }
}
