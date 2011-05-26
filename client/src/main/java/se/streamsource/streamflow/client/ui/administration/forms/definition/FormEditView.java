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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.forms.FormModel;
import se.streamsource.streamflow.client.util.BindingFormBuilder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.domain.form.FormValue;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;


/**
 * JAVADOC
 */
public class FormEditView
      extends JPanel
      implements Observer, Refreshable
{
   StateBinder formValueBinder;
   private ValueBuilderFactory vbf;
   private FormModel model;

   public FormEditView( @Service ApplicationContext context,
                        @Uses final CommandQueryClient client,
                        @Structure final ObjectBuilderFactory obf,
                        @Structure ValueBuilderFactory vbf )
   {
      super();

      this.model = obf.newObjectBuilder( FormModel.class ).use( client ).newInstance();
      setBorder(BorderFactory.createEmptyBorder());

      this.vbf = vbf;
      FormLayout formLayout = new FormLayout(
            "200dlu", "" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );
      formBuilder.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      formValueBinder = obf.newObject( StateBinder.class );
      formValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FormValue formValueTemplate = formValueBinder.bindingTemplate( FormValue.class );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, formValueBinder );

      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, formValueTemplate.description() ).
         appendLine( AdministrationResources.description_label, TEXTAREA, formValueTemplate.note() ).
         appendLine( AdministrationResources.form_id_label, TEXTFIELD, formValueTemplate.id() );

      formValueBinder.addObserver( this );

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      model.refresh();
      FormValue value = vbf.newValueBuilder( FormValue.class ).withPrototype( model.getFormValue() ).prototype();
      formValueBinder.updateWith( value );
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "description" ))
      {
         final ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( (String) property.get() );
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( builder.newInstance() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "note" ))
      {
         final ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( (String) property.get() );
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeNote( builder.newInstance() );
            }
         }.execute();
      }else if (property.qualifiedName().name().equals( "id" ))
      {
         final ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( (String) property.get() );
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeFormId( builder.newInstance() );
            }
         }.execute();
      }
   }
}