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
import org.qi4j.api.property.Property;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FieldValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.util.Observer;
import java.util.Observable;

/**
 * JAVADOC
 */
public class FieldValuePageBreakEditView
      extends JScrollPane
   implements Observer
{
   private StateBinder fieldDefinitionBinder;
   private StateBinder fieldValueBinder;
   private FieldValueEditModel model;



   public FieldValuePageBreakEditView( @Service ApplicationContext context,
                                     @Uses FieldValueEditModel model )
   {
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      this.model = model;
      FormLayout formLayout = new FormLayout(
            "200dlu", "" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setDefaultDialogBorder();
      fieldDefinitionBinder = new StateBinder();
      fieldDefinitionBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FieldDefinitionValue fieldDefinitionTemplate = fieldDefinitionBinder.bindingTemplate( FieldDefinitionValue.class );

      fieldValueBinder = new StateBinder();
      fieldValueBinder.setResourceMap( context.getResourceMap( getClass() ) );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, fieldDefinitionBinder );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.page_break_field_type ) ) );

      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, fieldDefinitionTemplate.description() ).
            appendLine( AdministrationResources.description_label, TEXTAREA, fieldDefinitionTemplate.note() );

      fieldValueBinder.addObserver( this );
      fieldDefinitionBinder.addObserver( this );

      fieldValueBinder.updateWith( model.getFieldDefinition().fieldValue().get() );
      fieldDefinitionBinder.updateWith( model.getFieldDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "mandatory" ))
      {
         try
         {
            model.changeMandatory( (Boolean) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( TaskResources.could_not_change_mandatory, e );
         }
      } else if (property.qualifiedName().name().equals( "description" ))
      {
         try
         {
            model.changeDescription( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( TaskResources.could_not_change_name, e );
         }
      } else if (property.qualifiedName().name().equals( "note" ))
      {
         try
         {
            model.changeNote( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( TaskResources.could_not_change_note, e );
         }
      }
   }

}