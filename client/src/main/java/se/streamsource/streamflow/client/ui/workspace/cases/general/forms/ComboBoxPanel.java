/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import javax.swing.JComboBox;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;

public class ComboBoxPanel
      extends AbstractFieldPanel
{
   private JComboBox box;

   public ComboBoxPanel( @Uses FieldSubmissionDTO field, @Uses ComboBoxFieldValue fieldValue )
   {
      super( field );
      setLayout( new BorderLayout( ) );

      List<String> values = new ArrayList<String>( fieldValue.values().get().size() +1 );
      values.add( 0, "" );
      values.addAll( 1, fieldValue.values().get() );

      box = new JComboBox( values.toArray() );
      box.setEditable( false );
      add( box, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      return (String) box.getSelectedItem();
   }

   @Override
   public void setValue( String newValue )
   {
      box.setSelectedItem( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      box.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            binding.updateProperty( getValue() );
         }
      });
   }
}