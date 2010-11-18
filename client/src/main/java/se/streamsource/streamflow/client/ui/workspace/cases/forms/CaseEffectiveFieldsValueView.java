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
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends JScrollPane
      implements TransactionListener, ListEventListener<EffectiveFieldDTO>
{
   @Service
   DialogService dialogs;

   private CaseEffectiveFieldsValueModel model;

   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) );

   private JPanel forms = new JPanel();

   private final ActionMap am;

   private Map<JButton, AttachmentFieldSubmission> attachmentButtons = new HashMap<JButton, AttachmentFieldSubmission>();

   @Structure
   ValueBuilderFactory vbf;

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      forms.setLayout( new BoxLayout( forms, BoxLayout.Y_AXIS ) );

      model = obf.newObjectBuilder( CaseEffectiveFieldsValueModel.class ).use( client ).newInstance();

      am = context.getActionMap( this );

      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );

      model.getEventList().addListEventListener( this );

      setViewportView( forms );

      new RefreshWhenVisible( this, model );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "submittedForm" ), transactions ))
      {
         model.refresh();
      }
   }

   public void listChanged( ListEvent<EffectiveFieldDTO> listEvent )
   {
      EventList<EffectiveFieldDTO> eventList = model.getEventList();
      eventList.getReadWriteLock().readLock().lock();
      try
      {
         forms.removeAll();
         Set<String> formNames = new LinkedHashSet<String>();
         for (EffectiveFieldDTO effectiveFieldDTO : eventList)
         {
            formNames.add( effectiveFieldDTO.formName().get() );
         }

         for (final String formName : formNames)
         {
            JPanel formPanel = new JPanel();
            FormLayout formLayout = new FormLayout("70dlu, 5dlu, 150dlu:grow","");
            DefaultFormBuilder builder = new DefaultFormBuilder( formLayout, formPanel );

            for (EffectiveFieldDTO effectiveFieldDTO : eventList)
            {
               if (effectiveFieldDTO.formName().get().equals(formName))
               {
                  String value = effectiveFieldDTO.fieldValue().get();
                  JComponent component;

                  if (effectiveFieldDTO.fieldType().get().equals( DateFieldValue.class.getName() ))
                  {
                     component = new JLabel( formatter.format( DateFunctions.fromString( value ) ) );
                  } else if (effectiveFieldDTO.fieldType().get().equals( TextAreaFieldValue.class.getName() ))
                  {
                     component = new JLabel( "<html>"+value.replace( "\n", "<br/>" )+"</html>" );
                  } else if ( effectiveFieldDTO.fieldType().get().equals( AttachmentFieldValue.class.getName() ))
                  {
                     final AttachmentFieldSubmission attachment = vbf.newValueFromJSON( AttachmentFieldSubmission.class, value );
                     JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                     panel.add( new JLabel(attachment.name().get()) );
                     JButton button = new JButton( am.get( "open" ) );
                     attachmentButtons.put( button, attachment );
                     /*button.addActionListener( new ActionListener()
                     {
                        public void actionPerformed( ActionEvent actionEvent )
                        {
                           new OpenAttachmentTask( attachment ).execute();
                        }
                     });*/
                     panel.add( button );
                     component = panel;
                  } else {
                     component = new JLabel( value );
                  }
                  builder.append( new JLabel(effectiveFieldDTO.fieldName().get()+":", SwingConstants.RIGHT), 1 );
                  component.setToolTipText( effectiveFieldDTO.submitter().get()+", "+formatter.format( effectiveFieldDTO.submissionDate().get() ) );
                  component.setBorder( BorderFactory.createEtchedBorder());
                  builder.append( component );
                  builder.nextLine();
               }
            }

            formPanel.setBorder( BorderFactory.createTitledBorder(formName ));

            forms.add( formPanel );
         }
         revalidate();
         repaint(  );
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }finally
      {
         eventList.getReadWriteLock().readLock().unlock();
      }
   }

   @Action
   public Task open( ActionEvent event ) throws IOException
   {
      AttachmentFieldSubmission selectedDocument = attachmentButtons.get( event.getSource() );
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

         Representation representation = model.download( attachment.attachment().get().identity() );

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
               dialogs.showMessageDialog( CaseEffectiveFieldsValueView.this, i18n.text( WorkspaceResources.could_not_open_attachment ), "" );
            }
         }
      }
   }
}