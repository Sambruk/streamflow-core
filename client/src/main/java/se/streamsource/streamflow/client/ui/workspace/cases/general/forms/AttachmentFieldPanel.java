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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.form.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * The panel responsible for viewing gui components for attaching a file from a form.
 */
public class AttachmentFieldPanel
      extends AbstractFieldPanel
{
   private JButton attach;
   private JLabel attachment = new JLabel();

   @Structure
   ValueBuilderFactory vbf;

   public AttachmentFieldPanel( @Uses FieldSubmissionValue field,
                                @Service ApplicationContext context )
   {
      super( field );

      FormLayout formLayout = new FormLayout(
            "60dlu, 2dlu, 150dlu",
            "20dlu" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );

      ApplicationActionMap am = context.getActionMap( this );
      attach = new JButton( am.get( "attach" ) );

      formBuilder.add( attach, "1,1,1,1" );
      formBuilder.add( attachment, "3,1,1,1" );
   }

   @Override
   public String getValue()
   {
      return attachment.getText();
   }

   @Action
   public void attach()
   {
      
   }

   @Override
   public void setValue( String newValue )
   {
      if (!newValue.equals(""))
      {
         AttachmentFieldSubmission submission = vbf.newValueFromJSON( AttachmentFieldSubmission.class, newValue );
         attachment.setText( submission.name().get() );
      }
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      attach.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled( false );

            if (fileChooser.showDialog( AttachmentFieldPanel.this, i18n.text( WorkspaceResources.create_attachment ) ) == JFileChooser.APPROVE_OPTION)
            {
               binding.updateProperty( fileChooser.getSelectedFile() );
            }

         }
      });
   }
}
