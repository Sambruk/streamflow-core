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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.form.*;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class FieldEditorNumberFieldValueView
      extends JScrollPane
{

   StateBinder fieldDefinitionBinder;
   StateBinder fieldValueBinder;

   public FieldEditorNumberFieldValueView( @Service ApplicationContext context,
                                           @Uses FieldValueEditModel model,
                                           @Structure ObjectBuilderFactory obf )
   {
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref, pref, pref, 5dlu, top:70dlu, pref" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setBorder( Borders.createEmptyBorder( "4dlu, 4dlu, 4dlu, 4dlu" ) );

      fieldDefinitionBinder = obf.newObject( StateBinder.class );
      fieldDefinitionBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FieldDefinitionValue fieldDefinitionTemplate = fieldDefinitionBinder.bindingTemplate( FieldDefinitionValue.class );

      fieldValueBinder = obf.newObject( StateBinder.class );
      fieldValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      NumberFieldValue fieldValueTemplate = fieldValueBinder.bindingTemplate( NumberFieldValue.class );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.number_field_type ) ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.mandatory ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( CHECKBOX.newField(), fieldDefinitionTemplate.mandatory() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.integer_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldValueBinder.bind( CHECKBOX.newField(), fieldValueTemplate.integer() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.name_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTFIELD.newField(), fieldDefinitionTemplate.description() ) );
      formBuilder.nextLine( 2 );

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.description_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTAREA.newField(), fieldDefinitionTemplate.note() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.field_id_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTFIELD.newField(), fieldDefinitionTemplate.fieldId() ) );

      FieldValueObserver observer = obf.newObjectBuilder( FieldValueObserver.class ).use( model ).newInstance();
      fieldValueBinder.addObserver( observer );
      fieldDefinitionBinder.addObserver( observer );

      fieldValueBinder.updateWith( model.getFieldDefinition().fieldValue().get() );
      fieldDefinitionBinder.updateWith( model.getFieldDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }
}