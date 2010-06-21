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
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JAVADOC
 */
public class AttachmentsView
      extends JPanel
{
   private JXTable attachments = new JXTable();

   private EventJXTableModel<AttachmentValue> tableModel;

   private AttachmentsModel attachmentsModel;
   public RefreshWhenVisible refresher;

   public void init( @Service ApplicationContext context )
   {
      setLayout( new BorderLayout() );

      ActionMap am = context.getActionMap( this );

      TableFormat tableFormat = new AttachmentsTableFormatter();

      tableModel = new EventJXTableModel<AttachmentValue>( new BasicEventList<AttachmentValue>(), tableFormat );

      TableColumn nameColumn = new TableColumn( 0 );
      nameColumn.setHeaderValue( tableFormat.getColumnName(0 ));
      TableColumn sizeColumn = new TableColumn( 1 );
      sizeColumn.setHeaderValue( tableFormat.getColumnName(1 ));
      TableColumn dateColumn = new TableColumn( 2 );
      dateColumn.setHeaderValue( tableFormat.getColumnName(2 ));

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

      if (fileChooser.showDialog( this, "Create attachment" ) == JFileChooser.APPROVE_OPTION)
      {
         attachmentsModel.createAttachment( fileChooser.getSelectedFile() );
         attachmentsModel.refresh();
      }
   }

   @Action
   public void remove()
   {
      for (int i : attachments.getSelectedRows())
      {
         AttachmentValue attachment = attachmentsModel.getEventList().get(attachments.convertRowIndexToModel( i ));
         attachmentsModel.removeAttachment(attachment);
      }
      attachmentsModel.refresh();
   }

   @Action
   public void open() throws IOException
   {
      for (int i : attachments.getSelectedRows())
      {
         AttachmentValue attachment = attachmentsModel.getEventList().get(attachments.convertRowIndexToModel( i ));
         String fileName = attachment.text().get();
         String[] fileNameParts = fileName.split( "\\." );
         File file = File.createTempFile( fileNameParts[0]+"_", "."+fileNameParts[1]);
         FileOutputStream out = new FileOutputStream(file);

         InputStream in = attachmentsModel.download(attachment);
         try
         {
            BioUtils.copy( in, out );
         } catch (IOException e)
         {
            in.close();
            out.close();
            throw e;
         }

         // Open file
         Desktop.getDesktop().edit( file );
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
