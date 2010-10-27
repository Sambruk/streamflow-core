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

package se.streamsource.streamflow.client.ui.workspace.table;

import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

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
      if (value.equals( CaseStates.OPEN ))
      {
         setIcon( i18n.icon( CaseResources.case_status_open_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( CaseResources.case_status_open_text ) );
         setToolTipText( i18n.text( CaseResources.case_status_draft_text ) );
      } else if (value.equals( CaseStates.DRAFT ))
      {
         setIcon( i18n.icon( CaseResources.case_status_draft_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( CaseResources.case_status_draft_text ) );
         setToolTipText( i18n.text( CaseResources.case_status_draft_text ) );
      } else if (value.equals( CaseStates.CLOSED ))
      {
         setIcon( i18n.icon( CaseResources.case_status_closed_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( CaseResources.case_status_closed_text ) );
         setToolTipText( i18n.text( CaseResources.case_status_closed_text ) );
      } else if (value.equals( CaseStates.ON_HOLD ))
      {
         setIcon( i18n.icon( CaseResources.case_status_on_hold_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( CaseResources.case_status_on_hold_text ) );
         setToolTipText( i18n.text( CaseResources.case_status_on_hold_text ) );
      }

      return this;
   }

}
