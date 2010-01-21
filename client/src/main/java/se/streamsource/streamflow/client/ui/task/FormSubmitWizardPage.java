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
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import org.jdesktop.swingx.JXDatePicker;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.common.Optional;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.FormDefinitionValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.Collections;

/**
 * JAVADOC
 */
public class FormSubmitWizardPage
      extends WizardPage
{
   private java.util.List<FieldSubmissionValue> fields;
   private java.util.Map<EntityReference, JComponent> componentFieldMap;
   private ValidationResultModel validationResultModel;

   public FormSubmitWizardPage(@Uses java.util.List<FieldSubmissionValue> fields,
                               @Uses Map wizardValueMap)
   {
      this.fields = fields;
      componentFieldMap = new HashMap<EntityReference, JComponent>();
      validationResultModel = new DefaultValidationResultModel();
      setLayout(new BorderLayout());
      JPanel panel = new JPanel( new FormLayout( ) );

      FormLayout formLayout = new FormLayout( "pref, 2dlu, pref:grow");

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      for (FieldSubmissionValue value : fields)
      {
         JComponent component;
         if ( value.field().get().fieldValue().get() instanceof TextFieldValue)
         {
            TextFieldValue field = (TextFieldValue) value.field().get().fieldValue().get();
            if ( field.rows().get() != null && field.rows().get() > 1)
            {
               component = new JTextArea( field.rows().get(),  field.width().get() );
               ((JTextArea)component).setText( value.value().get() );
            } else
            {
               component = new JTextField( field.width().get() );
               ((JTextField)component).setText( value.value().get() );
            }
         } else if ( value.field().get().fieldValue().get() instanceof DateFieldValue)
         {
            component = new JXDatePicker();
            JXDatePicker date = (JXDatePicker) component;
            SimpleDateFormat dateFormat = new SimpleDateFormat( i18n.text( WorkspaceResources.date_format ) );
            date.setFormats( dateFormat );
            if ( value.value().get() != null )
            {
               try
               {
                  date.setDate( dateFormat.parse( value.value().get() ) );
               } catch (ParseException e)
               {
                  e.printStackTrace();
               }
            }

         } else if ( value.field().get().fieldValue().get() instanceof NumberFieldValue)
         {
            NumberFieldValue field = (NumberFieldValue) value.field().get().fieldValue().get();

            NumberFormat numberInstance = NumberFormat.getNumberInstance();
            numberInstance.setParseIntegerOnly( field.integer().get() );
            component = new JFormattedTextField( numberInstance );
            ((JFormattedTextField)component).setText( value.value().get() );
         } else if ( value.field().get().fieldValue().get() instanceof SelectionFieldValue)
         {
            SelectionFieldValue field = (SelectionFieldValue) value.field().get().fieldValue().get();
            if ( field.multiple().get() )
            {
               component = new MultiSelectPanel( field.values().get() );
               ((MultiSelectPanel)component).setChecked( value.value().get() );
            } else
            {
               component = new JComboBox( field.values().get().toArray() );
               ((JComboBox)component).setSelectedItem( value.value().get() );
            }
         } else if ( value.field().get().fieldValue().get() instanceof CommentFieldValue )
         {
            CommentFieldValue field = (CommentFieldValue) value.field().get().fieldValue().get();
            component = new JLabel( field.comment().get() );
         } else
         {
            // Error!!!!
            component = new JTextField( );
         }

         // set tool tip
         if ( value.field().get().note().get().length() > 0 )
         {
            component.setToolTipText( value.field().get().note().get() );
         }

         componentFieldMap.put( value.field().get().field().get(), component );
         wizardValueMap.put( value.field().get().field().get().identity(), "" );
         StringBuilder componentName = new StringBuilder( "<html>" );

         componentName.append( value.field().get().description().get() );
         if ( value.field().get().fieldValue().get().mandatory().get() )
         {
            componentName.append( " <font color='red'>*</font>" );
         }
         componentName.append( "</html>" );
         formBuilder.append( componentName.toString(), component );
      }

      JComponent validationResultsComponent = ValidationResultViewFactory.createReportList(validationResultModel);
      formBuilder.appendRow("top:30dlu:g");

      CellConstraints cc = new CellConstraints();
      formBuilder.add(validationResultsComponent, cc.xywh(1, formBuilder.getRow() + 1, 3, 1, "fill, bottom"));

      JScrollPane scroll = new JScrollPane(panel);
      add(scroll,  BorderLayout.CENTER);
   }

   @Override
   public WizardPanelNavResult allowNext( String s, Map map, Wizard wizard )
   {
      mapValues( map );
      ValidationResult validation = validatePage( map );
      validationResultModel.setResult( validation );

      if ( validation.hasErrors())
      {
         return WizardPanelNavResult.REMAIN_ON_PAGE;
      } else
      {
         return WizardPanelNavResult.PROCEED;
      }
   }

   @Override
   public WizardPanelNavResult allowFinish( String s, Map map, Wizard wizard )
   {
      return allowNext( s, map, wizard );
   }


   private void mapValues( Map map )
   {
      for (FieldSubmissionValue field : fields)
      {
         JComponent component = componentFieldMap.get( field.field().get().field().get() );
         String value = "";
         if (component instanceof JTextField)
         {
            JTextField textField = (JTextField) component;
            value = textField.getText(  );
         } else if (component instanceof JTextArea)
         {
            JTextArea textArea = (JTextArea) component;
            value = textArea.getText();
         } else if (component instanceof JXDatePicker)
         {
            JXDatePicker datePicker = (JXDatePicker) component;
            value = datePicker.getDate()==null ? "" : datePicker.getEditor().getText(  );
         } else if (component instanceof JComboBox)
         {
            JComboBox box = (JComboBox) component;
            if ( box.getSelectedItem() != null)
            {
               value = box.getSelectedItem().toString();
            }
         } else if (component instanceof MultiSelectPanel)
         {
            MultiSelectPanel multiSelect = (MultiSelectPanel) component;
            value = multiSelect.getChecked();
         }
         map.put( field.field().get().field().get().identity(), value );
      }
   }

   private ValidationResult validatePage( Map map ) {
      ValidationResult validationResult = new ValidationResult();

      for (FieldSubmissionValue field : fields)
      {
         if ( field.field().get().fieldValue().get().mandatory().get() )
         {
            String value = (String) map.get( field.field().get().field().get().identity() );
            if (ValidationUtils.isEmpty( value ))
            {
               validationResult.addError( i18n.text(WorkspaceResources.mandatory_field_missing) + ": " + field.field().get().description().get() );
            }
         }
      }
      return validationResult;
   }

}