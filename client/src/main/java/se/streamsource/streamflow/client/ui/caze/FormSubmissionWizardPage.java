/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.caze;

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
import org.qi4j.api.property.Property;
import org.qi4j.api.util.DateFunctions;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;

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
import java.util.Map;
import java.util.HashMap;
import java.util.Observer;
import java.util.Observable;
import java.util.Date;

/**
 * JAVADOC
 */
public class FormSubmissionWizardPage
      extends WizardPage
   implements Observer
{
   private java.util.Map<FieldDefinitionValue, JComponent> componentFieldMap;
   private java.util.Map<StateBinder, EntityReference> fieldBinders;
   private ValidationResultModel validationResultModel;
   private FormSubmissionModel model;

   public FormSubmissionWizardPage( @Uses PageSubmissionValue page,
                                    @Uses FormSubmissionModel model)
   {
      super( page.title().get() );
      this.model = model;
      componentFieldMap = new HashMap<FieldDefinitionValue, JComponent>();
      validationResultModel = new DefaultValidationResultModel();
      setLayout(new BorderLayout());
      JPanel panel = new JPanel( new FormLayout( ) );

      fieldBinders = new HashMap<StateBinder, EntityReference>( page.fields().get().size() );
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );
      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, null );

      for (FieldSubmissionValue value : page.fields().get() )
      {
         JComponent component = null;
         if ( value.field().get().fieldValue().get() instanceof TextFieldValue)
         {
            TextFieldValue field = (TextFieldValue) value.field().get().fieldValue().get();
            if ( field.rows().get() != null && field.rows().get() > 1)
            {
               JScrollPane scroll = (JScrollPane) TEXTAREA.newField();
               JTextArea text = (JTextArea) scroll.getViewport().getView();
               text.setRows( field.rows().get());
               text.setColumns( field.width().get() );
               component = scroll;
            } else
            {
               component = new JTextField( field.width().get() );
            }
         } else if ( value.field().get().fieldValue().get() instanceof DateFieldValue)
         {
            component = new JXDatePicker();
         } else if ( value.field().get().fieldValue().get() instanceof NumberFieldValue)
         {
            NumberFieldValue field = (NumberFieldValue) value.field().get().fieldValue().get();

            NumberFormat numberInstance = NumberFormat.getNumberInstance();
            numberInstance.setParseIntegerOnly( field.integer().get() );
            component = new JFormattedTextField( numberInstance );
         } else if ( value.field().get().fieldValue().get() instanceof SelectionFieldValue)
         {
            SelectionFieldValue field = (SelectionFieldValue) value.field().get().fieldValue().get();
            if ( field.multiple().get() )
            {
               component = new MultiSelectPanel( field.values().get() );
            } else
            {
               component = new JComboBox( field.values().get().toArray() );
            }
         } else if ( value.field().get().fieldValue().get() instanceof CommentFieldValue )
         {
            bb.append( new JLabel( value.field().get().note().get() ) );
         }

         if ( component != null )
         {
            bindComponent( bb, value, component );
         }

      }

      JComponent validationResultsComponent = ValidationResultViewFactory.createReportList(validationResultModel);
      formBuilder.appendRow("top:30dlu:g");

      CellConstraints cc = new CellConstraints();
      formBuilder.add(validationResultsComponent, cc.xywh(1, formBuilder.getRow() + 1, 1, 1, "fill, bottom"));

      JScrollPane scroll = new JScrollPane(panel);
      add(scroll,  BorderLayout.CENTER);
   }

   private void bindComponent( BindingFormBuilder bb, FieldSubmissionValue value, JComponent component )
   {
      if ( value.field().get().note().get().length() > 0 )
      {
         component.setToolTipText( value.field().get().note().get() );
      }

      componentFieldMap.put( value.field().get(), component );

      StateBinder stateBinder = new StateBinder();
      FieldSubmissionValue value1 = stateBinder.bindingTemplate( FieldSubmissionValue.class );

      bb.append( getName( value ), component, value1.value(), stateBinder );

      fieldBinders.put( stateBinder, value.field().get().field().get() );
      stateBinder.addObserver( this );
      stateBinder.updateWith( value );
   }

   private String getName( FieldSubmissionValue value )
   {
      StringBuilder componentName = new StringBuilder( "<html>" );
      componentName.append( value.field().get().description().get() );
      if ( value.field().get().mandatory().get() )
      {
         componentName.append( " <font color='red'>*</font>" );
      }
      componentName.append( "</html>" );
      return componentName.toString();
   }

   @Override
   public WizardPanelNavResult allowNext( String s, Map map, Wizard wizard )
   {
      ValidationResult validation = validatePage( );
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

   private ValidationResult validatePage( ) {
      ValidationResult validationResult = new ValidationResult();


      for (Map.Entry<FieldDefinitionValue, JComponent> entry : componentFieldMap.entrySet())
      {

         JComponent component = entry.getValue();
         String value = "";
         if (component instanceof JTextField)
         {
            JTextField textField = (JTextField) component;
            value = textField.getText(  );
         } else if (component instanceof JScrollPane)
         {
            JTextArea textArea = (JTextArea) ((JScrollPane) component).getViewport().getView();
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

         if ( entry.getKey().mandatory().get() )
         {
            if (ValidationUtils.isEmpty( value ))
            {
               validationResult.addError( i18n.text(WorkspaceResources.mandatory_field_missing) + ": " + entry.getKey().description().get() );
            }
         }
      }
      return validationResult;
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if ( property.qualifiedName().name().equals( "value" ))
      {
         try
         {
            if ( property.get() instanceof Date)
            {
               model.updateField( fieldBinders.get( observable ), DateFunctions.toUtcString((Date)property.get()) );
            } else
            {
               model.updateField( fieldBinders.get( observable ), property.get().toString() );
            }
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_update_field, e );
         }
      }
   }
}