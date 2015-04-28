/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class OptionButtonsPanel
      extends AbstractFieldPanel
{

   private ButtonGroup group;

   public OptionButtonsPanel( @Uses FieldSubmissionDTO field, @Uses OptionButtonsFieldValue fieldValue )
   {
      super( field );
      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      group = new ButtonGroup();
      for ( String element : fieldValue.values().get() )
      {
         JRadioButton button = new JRadioButton( element );
         group.add( button );
         formBuilder.append( button );
         formBuilder.nextLine();
      }
      add( panel, BorderLayout.WEST );
   }


   @Override
   public String getValue()
   {
      Enumeration<AbstractButton> buttonEnumeration = group.getElements();
      while ( buttonEnumeration.hasMoreElements() )
      {
         AbstractButton button = buttonEnumeration.nextElement();
         if ( button.isSelected() ) return button.getText();
      }
      return "";
   }

   @Override
   public void setValue( String newValue )
   {
      Enumeration<AbstractButton> buttonEnumeration = group.getElements();
      while ( buttonEnumeration.hasMoreElements() )
      {
         AbstractButton button = buttonEnumeration.nextElement();
         if ( button.getText().equals( newValue ))
         {
            group.setSelected( button.getModel(), true );
         }
      }
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      ActionListener listener = new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            binding.updateProperty( getValue() );
         }
      };
      Enumeration<AbstractButton> buttonEnumeration = group.getElements();
      while ( buttonEnumeration.hasMoreElements() )
      {
         AbstractButton button = buttonEnumeration.nextElement();
         button.addActionListener( listener );
      }

   }
}