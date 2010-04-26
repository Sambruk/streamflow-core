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

import javax.swing.JTree;
import java.util.ArrayList;

/**
 * Helper for managing the expanded and selected rows in a tree. This is useful
 * for code that adds/removes nodes on the server and needs to have the tree updated
 * to reflect the changes.
 */
public class TreeState
{
   ArrayList<Integer> expandedRows = new ArrayList<Integer>();
   int[] selectedRows;

   JTree tree;

   public TreeState( JTree tree )
   {
      this.tree = tree;
   }

   public void save()
   {
      expandedRows.clear();
      for (int i = 0; i < tree.getRowCount(); i++)
      {
         if (tree.isExpanded( i ))
            expandedRows.add( i );
      }
      selectedRows = tree.getSelectionRows();
   }

   public void restore()
   {
      for (int i = 0; i < tree.getRowCount(); i++)
      {
         if (expandedRows.contains( i ))
            tree.expandRow( i );
         else
            tree.collapseRow( i );
      }

      tree.setSelectionRows( selectedRows );
   }
}
