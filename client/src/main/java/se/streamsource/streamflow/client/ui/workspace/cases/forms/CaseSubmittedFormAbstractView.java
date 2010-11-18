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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.EventList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public abstract class CaseSubmittedFormAbstractView
      extends JScrollPane
   implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   ValueBuilderFactory vbf;

   @Service
   ApplicationContext context;

   private JPanel panel;

   public CaseSubmittedFormAbstractView()
   {
      panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
      setViewportView( panel );

      setMinimumSize( new Dimension( 150, 0 ) );
   }


   private Map<JButton, AttachmentFieldSubmission> buttons = new HashMap<JButton, AttachmentFieldSubmission>();

   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) );

   abstract protected FormAttachmentDownload getModel();

   public JComponent getComponent( String fieldValue, String fieldType )
   {
      JComponent component;
      if ( fieldType.equals( DateFieldValue.class.getName() ))
      {
         component = new JLabel( formatter.format( DateFunctions.fromString( fieldValue ) ) );
      } else if ( fieldType.equals( TextAreaFieldValue.class.getName() ))
      {
         component = new JLabel( "<html>"+fieldValue.replace( "\n", "<br/>" )+"</html>" );
      } else if ( fieldType.equals( AttachmentFieldValue.class.getName() ))
      {
         final AttachmentFieldSubmission attachment = vbf.newValueFromJSON( AttachmentFieldSubmission.class, fieldValue );
         JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
         panel.add( new JLabel(attachment.name().get()) );
         JButton button = new JButton( context.getActionMap(this).get( "open" ) );
         buttons.put( button, attachment );
         panel.add( button );
         component = panel;
      } else {
         component = new JLabel( fieldValue );
      }
      component.setBorder( BorderFactory.createEtchedBorder() );
      return component;
   }

   public Task openAttachment( ActionEvent event )
   {
      AttachmentFieldSubmission selectedDocument = buttons.get( event.getSource() );
      return new OpenAttachmentTask( selectedDocument );
   }

   private class OpenAttachmentTask extends Task<File, Void>
   {
      private final AttachmentFieldSubmission attachment;

      public OpenAttachmentTask( AttachmentFieldSubmission attachment )
      {
         super( Application.getInstance() );
         this.attachment = attachment;

         setUserCanCancel( false );
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         String fileName = attachment.name().get();
         String[] fileNameParts = fileName.split( "\\." );

         Representation representation = getModel().download( attachment.attachment().get().identity() );

         File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );

         Inputs.byteBuffer( representation.getStream(), 8192 ).transferTo( Outputs.byteBuffer(file ));

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
               dialogs.showMessageDialog( CaseSubmittedFormAbstractView.this, i18n.text( WorkspaceResources.could_not_open_attachment ), "" );
            }
         }
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "submittedForm" ), transactions ))
      {
         getModel().refresh();
      }
   }

   protected JPanel panel()
   {
      return panel;
   }

   protected <T> void safeRead(EventList<T> eventList, EventCallback<T> callback )
   {
      eventList.getReadWriteLock().readLock().lock();
      try
      {
         for (T t : eventList)
         {
            callback.iterate( t );
         }
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }finally
      {
         eventList.getReadWriteLock().readLock().unlock();
      }
   }

   protected interface EventCallback<T>
   {
      void iterate(T t);
   }

   protected DefaultFormBuilder builder(JPanel aPanel )
   {
      FormLayout formLayout = new FormLayout("70dlu, 5dlu, 150dlu:grow","");
      return new DefaultFormBuilder( formLayout, aPanel );
   }
}