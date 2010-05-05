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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.caze.CaseResources;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.CHECKBOX;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;

/**
 * JAVADOC
 */
public class PageEditView
      extends JScrollPane
      implements Observer
{
   StateBinder nameBinder;
   private PageEditModel model;

   public PageEditView( @Service ApplicationContext context,
                        @Uses PageEditModel model)
   {
      this.model = model;
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));
      
      nameBinder = new StateBinder();
      nameBinder.setResourceMap( context.getResourceMap( getClass() ) );
      PageDefinitionValue definitionValue = nameBinder.bindingTemplate( PageDefinitionValue.class );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, nameBinder );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.page_break_field_type ) ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.name_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(nameBinder.bind( TEXTFIELD.newField(), definitionValue.description() ) );
      
//      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.page_break_field_type ) ) );
//
//      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, definitionValue.description() );

      nameBinder.addObserver( this );
      nameBinder.updateWith( model.getPageDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "description" ))
      {
         try
         {
            model.changeDesctiption( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_description, e );
         }
      }
   }
}