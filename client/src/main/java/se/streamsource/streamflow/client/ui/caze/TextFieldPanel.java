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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextFieldPanel
      extends AbstractFieldPanel
{
   private JTextField textField;

   public TextFieldPanel( @Uses FieldSubmissionValue field, @Uses TextFieldValue fieldValue )
   {
      super( field );

      textField = new JTextField();
      textField.setColumns( fieldValue.width().get() );
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
      textField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify( JComponent input )
         {
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      });
   }
}