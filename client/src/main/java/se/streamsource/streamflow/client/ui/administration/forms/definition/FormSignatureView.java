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
import org.qi4j.api.property.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class FormSignatureView
      extends JPanel
      implements Observer, Refreshable, TransactionListener
{
   private FormSignatureModel model;
   @Structure
   ValueBuilderFactory vbf;
   private StateBinder formValueBinder;

   public FormSignatureView( @Service ApplicationContext context,
                             @Uses CommandQueryClient client,
                             @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      this.model = obf.newObjectBuilder( FormSignatureModel.class ).use( client ).newInstance();

      FormLayout formLayout = new FormLayout(
            "200dlu", "" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );
      formBuilder.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      formValueBinder = obf.newObject( StateBinder.class );
      formValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      RequiredSignatureValue signatureValue = formValueBinder.bindingTemplate( RequiredSignatureValue.class );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, formValueBinder );

      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, signatureValue.name() ).
            appendLine( AdministrationResources.description_label, TEXTFIELD, signatureValue.description() );

      formValueBinder.addObserver( this );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();
      RequiredSignatureValue value = vbf.newValueBuilder( RequiredSignatureValue.class ).withPrototype( model.getFormSignature() ).prototype();
      formValueBinder.updateWith( value );
   }

   public void update( Observable observable, Object arg )
   {
      final ValueBuilder<RequiredSignatureValue> builder = vbf.newValueBuilder( RequiredSignatureValue.class ).withPrototype( model.getFormSignature() );
      final Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "description" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               builder.prototype().description().set( (String) property.get() );
               model.update( builder.newInstance() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "name" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               builder.prototype().name().set( (String) property.get() );
               model.update( builder.newInstance() );
            }
         }.execute();
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {

      model.notifyTransactions( transactions );
   }
}
