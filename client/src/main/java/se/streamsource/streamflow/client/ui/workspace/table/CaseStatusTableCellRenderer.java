/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.table;

import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CaseStatusTableCellRenderer extends DefaultTableCellRenderer
{
   public CaseStatusTableCellRenderer()
   {
   }

   public Component getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column )
   {

      JLabel renderedComponent = (JLabel) super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
            row, column );
      renderedComponent.setHorizontalAlignment( SwingConstants.CENTER );
      setText( null );

      setIcon( i18n.icon( CaseResources.valueOf( "case_status_" + value.toString().toLowerCase() + "_icon" ),
            i18n.ICON_16 ) );
      setName( i18n.text( CaseResources.valueOf("case_status_"+value.toString().toLowerCase()+"_text" ) ));
      setToolTipText( i18n.text( CaseResources.valueOf("case_status_"+value.toString().toLowerCase()+"_text" ) ) );

      return this;
   }

}
