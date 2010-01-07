/**
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package se.streamsource.streamflow.client.ui.task;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.util.List;
import java.util.ArrayList;
import java.awt.FlowLayout;

public class MultiSelectPanel extends JPanel
{

   List<JCheckBox> checkBoxes;

   public MultiSelectPanel( List<String> elements )
   {
      super(new FlowLayout( FlowLayout.LEFT ) );

      checkBoxes = new ArrayList<JCheckBox>();
      for (String element : elements)
      {
         JCheckBox checkBox = new JCheckBox( element );
         checkBoxes.add(checkBox);
         add( checkBox );
      }
   }

   public String getChecked()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (JCheckBox checkBox : checkBoxes)
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
}
