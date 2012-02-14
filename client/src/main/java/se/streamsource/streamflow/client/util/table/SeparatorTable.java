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
package se.streamsource.streamflow.client.util.table;

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventTableModel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;

/**
 * JAVADOC
 */
public class SeparatorTable extends JXTable
{
   private final Class separatorClass;

   public SeparatorTable( EventTableModel model )
   {
      super( model );
      this.separatorClass = SeparatorList.Separator.class;
      setColumnSelectionAllowed( false );
      setUI( new SeparatorTableUI( separatorClass ) );
      getTableHeader().setReorderingAllowed( false );
      setSortable( false );
      setRowSorter( null );
      setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
   }


   public Rectangle getCellRect( int row, int column, boolean includeSpacing )
   {
      Rectangle sRect = super.getCellRect( row, column, includeSpacing );
      if ((row < 0) || (column < 0) ||
            (getModel().getRowCount() <= row) || (getModel().getColumnCount() <= column))
      {
         return sRect;
      }

      int index = 0;
      int columnMargin = getColumnModel().getColumnMargin();
      Rectangle cellFrame = new Rectangle();
      int aCellHeight = rowHeight + rowMargin;
      cellFrame.y = row * aCellHeight;
      cellFrame.height = aCellHeight;

      Enumeration enumeration = getColumnModel().getColumns();
      while (enumeration.hasMoreElements())
      {
         TableColumn aColumn = (TableColumn) enumeration.nextElement();
         cellFrame.width = aColumn.getWidth() + columnMargin;
         if (index == column) break;
         cellFrame.x += cellFrame.width;
         index++;
      }

      EventTableModel model = (EventTableModel) getModel();
      if (separatorClass.isInstance( model.getElementAt( row ) ))
      {
         while (enumeration.hasMoreElements())
         {
            TableColumn aColumn = (TableColumn) enumeration.nextElement();
            cellFrame.width += aColumn.getWidth() + columnMargin;
         }
      }

      if (!includeSpacing)
      {
         Dimension spacing = getIntercellSpacing();
         cellFrame.setBounds( cellFrame.x + spacing.width / 2,
               cellFrame.y + spacing.height / 2,
               cellFrame.width - spacing.width,
               cellFrame.height - spacing.height );
      }
      return cellFrame;
   }


   private int[] rowColumnAtPoint( Point point )
   {
      int[] retValue = {-1, -1};
      int row = point.y / (rowHeight + rowMargin);
      if ((row < 0) || (getModel().getRowCount() <= row)) return retValue;
      int column = getColumnModel().getColumnIndexAtX( point.x );

      if (getModel().getRowCount() == 0)
         return retValue;

      if (separatorClass.isInstance(((EventTableModel)getModel()).getElementAt( row )))
         column = 0;

      return new int[]{row,column};
   }


   public int rowAtPoint( Point point )
   {
      return rowColumnAtPoint( point )[0];
   }

   public int columnAtPoint( Point point )
   {
      return rowColumnAtPoint( point )[1];
   }

   public void columnSelectionChanged( ListSelectionEvent e )
   {
      repaint();
   }

   public void valueChanged( ListSelectionEvent e )
   {
      int firstIndex = e.getFirstIndex();
      int lastIndex = e.getLastIndex();
      if (firstIndex == -1 && lastIndex == -1)
      { // Selection cleared.
         repaint();
      }
      Rectangle dirtyRegion = getCellRect( firstIndex, 0, false );
      int numCoumns = getColumnCount();
      int index = firstIndex;
      for (int i = 0; i < numCoumns; i++)
      {
         dirtyRegion.add( getCellRect( index, i, false ) );
      }
      index = lastIndex;
      for (int i = 0; i < numCoumns; i++)
      {
         dirtyRegion.add( getCellRect( index, i, false ) );
      }
      repaint( dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height );
   }

}
