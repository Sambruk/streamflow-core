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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class FieldValueDateEditView
      extends JScrollPane
{

   StateBinder fieldDefinitionBinder;
   StateBinder fieldValueBinder;

   public FieldValueDateEditView( @Service ApplicationContext context,
                                  @Uses FieldValueEditModel model,
                                  @Structure ObjectBuilderFactory obf)
   {
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "200dlu", "" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setDefaultDialogBorder();
      fieldDefinitionBinder = new StateBinder();
      fieldDefinitionBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FieldDefinitionValue fieldDefinitionTemplate = fieldDefinitionBinder.bindingTemplate( FieldDefinitionValue.class );

      fieldValueBinder = new StateBinder();
      fieldValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      DateFieldValue fieldValueTemplate = fieldValueBinder.bindingTemplate( DateFieldValue.class );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, fieldDefinitionBinder );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.date_field_type ) ) );

      bb.appendLine( AdministrationResources.mandatory, CHECKBOX, fieldDefinitionTemplate.mandatory() ).
            appendLine( AdministrationResources.name_label, TEXTFIELD, fieldDefinitionTemplate.description() ).
            appendLine( AdministrationResources.description_label, TEXTAREA, fieldDefinitionTemplate.note() );

      FieldValueObserver observer = obf.newObjectBuilder( FieldValueObserver.class ).use( model ).newInstance();
      fieldValueBinder.addObserver( observer );
      fieldDefinitionBinder.addObserver( observer );

      fieldValueBinder.updateWith( model.getFieldDefinition().fieldValue().get() );
      fieldDefinitionBinder.updateWith( model.getFieldDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }
}