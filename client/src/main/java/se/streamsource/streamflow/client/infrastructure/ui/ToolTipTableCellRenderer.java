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

package se.streamsource.streamflow.client.infrastructure.ui;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class ToolTipTableCellRenderer extends DefaultTableCellRenderer
{
   @Override
   public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
   {
      String s = value.toString();
      JComponent component = (JComponent) super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
      if (s.contains( "\n" ) || s.length() > 25 )
      {
         StringBuilder sb = new StringBuilder( "<html>" );
         for (String line : s.split( "\n" ))
         {
            sb.append( "<p>" );
            sb.append( line );
            sb.append( "</p>" );
         }
         sb.append( "</html>" );
         component.setToolTipText( sb.toString() );
      } else
      {
         component.setToolTipText( "" );
      }
      return component;
   }
}