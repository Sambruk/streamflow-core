/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.NumberFormat;

/**
 * JAVADOC
 */
public class FormSubmitWizardPage
      extends WizardPage
{
   public FormSubmitWizardPage(@Uses java.util.List<FieldDefinitionValue> fields,
                               @Uses Map wizardValueMap)
   {
      setLayout(new BorderLayout());
      JPanel panel = new JPanel( new FormLayout( ) );

      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      for (FieldDefinitionValue value : fields)
      {
         JComponent component;
         if (value.fieldValue().get() instanceof TextFieldValue)
         {
            TextFieldValue field = (TextFieldValue) value.fieldValue().get();
            if ( field.rows().get() != null && field.rows().get() > 1)
            {
               component = new JTextArea( field.rows().get(),  field.width().get() );
            } else
            {
               component = new JTextField( field.width().get() );
            }
         } else if ( value.fieldValue().get() instanceof DateFieldValue)
         {
            component = new JXDatePicker();
         } else if ( value.fieldValue().get() instanceof NumberFieldValue)
         {
            NumberFieldValue field = (NumberFieldValue) value.fieldValue().get();
            
            NumberFormat numberInstance = NumberFormat.getNumberInstance();
            numberInstance.setParseIntegerOnly( field.integer().get() );
            component = new JFormattedTextField( numberInstance );
         } else if ( value.fieldValue().get() instanceof SelectionFieldValue)
         {
            SelectionFieldValue field = (SelectionFieldValue) value.fieldValue().get();
            if ( field.multiple().get() )
            {
               component = new MultiSelectPanel( field.values().get() );
            } else
            {
               component = new JComboBox( field.values().get().toArray() );
            }
         } else if ( value.fieldValue().get() instanceof CommentFieldValue )
         {
           CommentFieldValue field = (CommentFieldValue) value.fieldValue().get();
           component = new JLabel( field.comment().get() );
         } else
         {
            component = new JTextField( );
         }

         // set tool tip
         if (value.note().get().length() > 0 )
         {
            component.setToolTipText( value.note().get() );
         }

         wizardValueMap.put( value.field().get().identity(), component );
         formBuilder.append( value.description().get(), component );
      }
      JScrollPane scroll = new JScrollPane(panel);
      add(scroll,  BorderLayout.CENTER);
   }

   private WizardPanelNavResult forward( Map map )
   {
      for (Object o : map.entrySet())
      {
         Object value = ((Map.Entry) o).getValue();
         if (value instanceof TextField)
         {
            String id = (String) ((Map.Entry) o).getKey();
            String fieldValue = ((TextField) map.get( id )).getText();
            if (fieldValue == null || fieldValue.equals( "" ))
            {
               setProblem( i18n.text( WorkspaceResources.fill_all_fields));
               return WizardPanelNavResult.REMAIN_ON_PAGE;
            }
         }
      }
      return WizardPanelNavResult.PROCEED;
   }
}