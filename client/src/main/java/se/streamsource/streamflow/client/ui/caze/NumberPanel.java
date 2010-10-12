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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class NumberPanel
       extends AbstractFieldPanel
{
   private JTextField textField;

   @Service
   DialogService dialogs;
   private Boolean isInteger;

   public NumberPanel( @Uses FieldSubmissionValue field, @Uses NumberFieldValue fieldValue )
   {
      super( field );
      setLayout( new BorderLayout( ) );

      textField = new JTextField();
      textField.setColumns( 20 );
      this.isInteger = fieldValue.integer().get();
      add( textField, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue( String newValue )
   {
      textField.setText( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      final NumberPanel panel = this;
      textField.setInputVerifier( new InputVerifier()
      {

         @Override
         public boolean verify( JComponent input )
         {
            JTextField field = (JTextField) input;
            try
            {
               String value = field.getText();
               if ( isInteger)
               {
                  binding.updateProperty( Integer.parseInt( value ) );
               } else
               {
                  binding.updateProperty( Double.parseDouble( value.replace( ',', '.' ) ) );
               }
            }  catch ( NumberFormatException e)
            {
               if ( isInteger )
               {
                  dialogs.showMessageDialog( panel , i18n.text( CaseResources.invalidinteger ), ""  );
               } else
               {
                  dialogs.showMessageDialog( panel, i18n.text( CaseResources.invalidfloat ), "");
               }
               return false;
            }
            return true;
         }
      });
   }
}