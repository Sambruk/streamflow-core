/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.projects.members;

import se.streamsource.dci.value.LinkValue;

import javax.swing.event.TableModelEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TableMultipleSelectionModel
      extends AbstractTableSelectionModel<Set<String>>
{
   private Map<LinkValue, Boolean> selected;

   public void clearSelection()
   {
      selected = new HashMap<LinkValue, Boolean>();
      fireTableChanged( new TableModelEvent( this, 0, getRowCount(), 0, TableModelEvent.DELETE ) );
   }

   public Set<String> getSelected()
   {
      Set<String> selectedIdentities = new LinkedHashSet<String>();

      for (LinkValue linkValue : selected.keySet())
      {
         selectedIdentities.add( linkValue.id().get() );
      }
      return selectedIdentities;
   }


   public Object getValueAt( int row, int column )
   {
      switch (column)
      {
         case 0:
            return selected.get( getModel().links().get().get( row ) );
         case 1:
            return getModel().links().get().get( row ).text().get();
      }
      return null;
   }

   @Override
   public void setValueAt( Object value, int row, int column )
   {
      if (!(Boolean) value)
      {
         selected.remove( getModel().links().get().get( row ) );
      } else
      {
         selected.put( getModel().links().get().get( row ), Boolean.TRUE );
      }
      fireTableCellUpdated( row, column );
   }

}