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

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import java.text.DateFormat;
import java.util.EventListener;
import java.util.Locale;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * Abstract class that each field type must extend
 */
public abstract class AbstractFieldPanel extends JPanel
{
   private FieldSubmissionValue field;
   protected StateBinder.Binding binding;

   @Structure
   ObjectBuilderFactory obf;

   public AbstractFieldPanel( FieldSubmissionValue field )
   {
      this.field = field;
   }

   abstract public String getValue();

   abstract public void setValue( String newValue );

   abstract public boolean validateValue( Object newValue );

   public StateBinder bindComponent( BindingFormBuilder bb, FieldSubmissionValue value )
   {
      if ( value.field().get().note().get().length() > 0 )
      {
         setToolTipText( value.field().get().note().get() );
      }

      StateBinder stateBinder = obf.newObject( StateBinder.class );
      FieldSubmissionValue value1 = stateBinder.bindingTemplate( FieldSubmissionValue.class );

      bb.append( componentName(), this, value1.value(), stateBinder );

      stateBinder.updateWith( value );
      return stateBinder;
   }

   public String title()
   {
      return field.field().get().description().get();
   }

   public boolean mandatory()
   {
      return field.field().get().mandatory().get();
   }

   private String componentName( )
   {
      StringBuilder componentName = new StringBuilder( "<html>" );
      componentName.append( title() );
      if ( mandatory() )
      {
         componentName.append( " <font color='red'>*</font>" );
      }
      componentName.append( "</html>" );
      return componentName.toString();
   }


   abstract public void setBinding( StateBinder.Binding binding );
}