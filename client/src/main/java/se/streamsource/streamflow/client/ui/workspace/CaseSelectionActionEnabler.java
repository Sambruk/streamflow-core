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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.swingx.JXTable;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;

import javax.swing.Action;

/**
 * Action enabler that checks whether the completed flag is set or not
 */
public class CaseSelectionActionEnabler
      extends SelectionActionEnabler
{
   private int completedColumn;
   private JXTable table;

   public CaseSelectionActionEnabler( int completedColumn, JXTable table, Action... action )
   {
      super( action );
      this.completedColumn = completedColumn;
      this.table = table;
   }

   @Override
   public boolean isSelectedValueValid(Action action)
   {
      return !table.getValueAt( table.convertRowIndexToModel( table.getSelectedRow() ), completedColumn ).equals( CaseStates.CLOSED );
   }
}
