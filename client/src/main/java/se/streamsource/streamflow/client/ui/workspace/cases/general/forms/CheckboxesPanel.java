/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class CheckboxesPanel
      extends AbstractFieldPanel
{

   Map<String, JCheckBox> checkBoxMap;

   public CheckboxesPanel( @Uses FieldSubmissionValue field, @Uses CheckboxesFieldValue fieldValue )
   {
      super( field );

      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      checkBoxMap = new HashMap<String, JCheckBox>();
      for ( String element : fieldValue.values().get() )
      {
         JCheckBox checkBox = new JCheckBox( element );
         checkBoxMap.put( element, checkBox );
         formBuilder.append( checkBox );
         formBuilder.nextLine();
      }

      add( panel, BorderLayout.WEST );
   }

   @Override
   public void setValue( String newValue )
   {
      if ( newValue == null || newValue.equals( "" )) return;
      String[] boxes = newValue.split( ", " );
      for (String box : boxes)
      {
         checkBoxMap.get( box ).setSelected( true );
      }
   }

   @Override
   public String getValue()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (JCheckBox checkBox : checkBoxMap.values())
      {
         if ( checkBox.isSelected() )
         {
            if (!first) sb.append( ", " );
            sb.append( checkBox.getText() );
            first = false;
         }
      }
      return sb.toString();
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      ActionListener listener = new ActionListener() {
         public void actionPerformed( ActionEvent e )
         {
            binding.updateProperty( getValue()  );
         }
      };
      for (JCheckBox box : checkBoxMap.values())
      {
         box.addActionListener( listener );
      }
   }
}
