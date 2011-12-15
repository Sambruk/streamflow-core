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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.util.Strings;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class OpenSelectionPanel
      extends AbstractFieldPanel
{

   private ButtonGroup group;
   private JRadioButton openButton;
   private List<JRadioButton> selectionButtons;
   private JTextField openSelectionTextField;

   public OpenSelectionPanel( @Uses FieldSubmissionDTO field, @Uses OpenSelectionFieldValue fieldValue )
   {
      super( field );
      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      group = new ButtonGroup();
      selectionButtons = new ArrayList<JRadioButton>( fieldValue.values().get().size() ); 
      for ( String element : fieldValue.values().get() )
      {
         JRadioButton button = new JRadioButton( element );
         group.add( button );
         selectionButtons.add( button );
         formBuilder.append( button );
         formBuilder.nextLine();
      }

      // add the "open" selection
      JPanel openPanel = new JPanel( new BorderLayout( ));
      openButton = new JRadioButton( fieldValue.openSelectionName().get() );
      openSelectionTextField = new JTextField( );
      openPanel.add( openButton, BorderLayout.WEST );
      openPanel.add( openSelectionTextField, BorderLayout.CENTER );
      group.add( openButton );
      formBuilder.append( openPanel );

      setValue( field.value().get() );
      add( panel, BorderLayout.WEST );
   }


   @Override
   public String getValue()
   {

      if ( openButton.isSelected() )
      {
         return openSelectionTextField.getText();
      } else {
         for (JRadioButton selectionButton : selectionButtons)
         {
            if ( selectionButton.isSelected() )
            {
               return selectionButton.getText();
            }
         }
      }
      return "";
   }

   @Override
   public void setValue( String newValue )
   {
      for (JRadioButton selectionButton : selectionButtons)
      {
         if ( selectionButton.getText().equals( newValue ))
         {
            openSelectionTextField.setText( "" );
            openSelectionTextField.setEnabled( false );
            selectionButton.setSelected( true );
            return;
         }
      }
      // if we reach here it must be the "other" option that is selected
      // unless it is the empty string
      if ( !Strings.empty( newValue ) )
      {
         openButton.setSelected( true );
         openSelectionTextField.setText( newValue );
         openSelectionTextField.setEnabled( true );
      }
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      ActionListener listener = new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            openSelectionTextField.setText( "" );
            openSelectionTextField.setEnabled( false );
            binding.updateProperty( getValue() );
         }
      };
      openButton.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            openSelectionTextField.setEnabled( true );
         }
      } );
      openSelectionTextField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify( JComponent input )
         {
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      });

      for (JRadioButton selectionButton : selectionButtons)
      {
         selectionButton.addActionListener( listener );
      }
   }
}