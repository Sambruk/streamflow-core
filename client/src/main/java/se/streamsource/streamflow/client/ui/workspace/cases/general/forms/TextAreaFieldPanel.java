/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTAREA;

import java.awt.BorderLayout;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;

public class TextAreaFieldPanel
      extends AbstractFieldPanel
{
   private JTextArea text;

   public TextAreaFieldPanel( @Uses FieldSubmissionDTO field, @Uses TextAreaFieldValue fieldValue )
   {
      super( field );
      setLayout( new BorderLayout( ) );

      JScrollPane scroll = (JScrollPane) TEXTAREA.newField();
      text = (JTextArea) scroll.getViewport().getView();
      text.setRows( fieldValue.rows().get());
      text.setColumns( fieldValue.cols().get() );
      add( scroll, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      return text.getText();
   }

   @Override
   public void setValue( String newValue )
   {
      text.setText( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      text.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify( JComponent input )
         {
            binding.updateProperty( ((JTextComponent)input).getText() );
            return true;
         }
      });
   }
}