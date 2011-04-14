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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;

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
                        @Uses CommandQueryClient client,
                        @Structure ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( PageEditModel.class ).use( client ).newInstance();
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));
      
      nameBinder = obf.newObject( StateBinder.class );
      nameBinder.setResourceMap( context.getResourceMap( getClass() ) );
      PageDefinitionValue definitionValue = nameBinder.bindingTemplate( PageDefinitionValue.class );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.page_break_field_type ) ) );
      formBuilder.nextLine();

      formBuilder.add(new JLabel(i18n.text(AdministrationResources.name_label)));
      formBuilder.nextColumn(2);
      formBuilder.add(nameBinder.bind( TEXTFIELD.newField(), definitionValue.description() ) );
      
//      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.page_break_field_type ) ) );
//
//      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, definitionValue.description() );

      nameBinder.addObserver( this );
      model.refresh();
      nameBinder.updateWith( model.getPageDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }

   public void update( Observable observable, Object arg )
   {
      final Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "description" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( (String) property.get() );
            }
         }.execute();
      }
   }
}