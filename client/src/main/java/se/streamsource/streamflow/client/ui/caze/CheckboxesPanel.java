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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckboxesPanel extends JPanel
{

   Map<String, JCheckBox> checkBoxMap;

   public CheckboxesPanel( List<String> elements )
   {
      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      checkBoxMap = new HashMap<String, JCheckBox>();
      for (String element : elements)
      {
         JCheckBox checkBox = new JCheckBox( element );
         checkBoxMap.put( element, checkBox );
         formBuilder.append( checkBox );
         formBuilder.nextLine();
      }

      add( panel, BorderLayout.CENTER );
   }

   public void addActionPerformedListener( ActionListener listener )
   {
      for (JCheckBox box : checkBoxMap.values())
      {
         box.addActionListener( listener );
      }
   }

   public String getChecked()
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

   public void setChecked( String checked )
   {
      if ( checked == null) return;
      String[] boxes = checked.split( ", " );
      for (String box : boxes)
      {
         checkBoxMap.get( box ).setSelected( true );
      }
   }
}
