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

package se.streamsource.streamflow.client.util.table;

import ca.odell.glazedlists.swing.EventTableModel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * JAVADOC
 */
public class SeparatorTableUI extends BasicTableUI
{
   private Class separatorClass;

   public SeparatorTableUI( Class separatorClass )
   {
      this.separatorClass = separatorClass;
   }

   public void paint( Graphics g, JComponent c )
   {
      Rectangle oldClipBounds = g.getClipBounds();

      Rectangle bounds = table.getBounds();
      // account for the fact that the graphics has already been translated
      // into the table's bounds
      bounds.x = bounds.y = 0;

      if (table.getModel().getRowCount() <= 0 ||
            // this check prevents us from painting the entire table
            // when the clip doesn't intersect our bounds at all
            !bounds.intersects( oldClipBounds ))
      {
         return;
      }

      Rectangle clipBounds = new Rectangle( oldClipBounds );
      int tableWidth = table.getColumnModel().getTotalColumnWidth();
      clipBounds.width = Math.min( clipBounds.width, tableWidth );
      g.setClip( clipBounds );

      int firstIndex = table.rowAtPoint( new Point( 0, clipBounds.y ) );
      int lastIndex = table.getModel().getRowCount() - 1;

      Rectangle rowRect = new Rectangle( 0, 0,
            tableWidth, table.getRowHeight() + table.getRowMargin() );
      rowRect.y = firstIndex * rowRect.height;

      for (int index = firstIndex; index <= lastIndex; index++)
      {
         if (rowRect.intersects( clipBounds ))
         {
            //System.out.println();                  // debug
            //System.out.print("" + index +": ");    // row
            paintRow( g, index );
         }
         rowRect.y += rowRect.height;
      }
      g.setClip( oldClipBounds );
   }

   private void paintRow( Graphics g, int row )
   {
      Rectangle rect = g.getClipBounds();

      EventTableModel tableModel = (EventTableModel) table.getModel();
      int numColumns = table.getColumnCount();

      if (tableModel.getRowCount()>row && separatorClass.isInstance( tableModel.getElementAt( row ) ))
      {
         Rectangle cellRect = table.getCellRect( row, 0, true );
         paintCell( g, cellRect, row, 0 );
      } else
      {
         for (int column = 0; column < numColumns; column++)
         {
            Rectangle cellRect = table.getCellRect( row, column, true );

            if (cellRect.intersects( rect ))
            {
               paintCell( g, cellRect, row, column );
            }
         }
      }

   }

   private void paintCell( Graphics g, Rectangle cellRect, int row, int column )
   {
      int spacingHeight = table.getRowMargin();
      int spacingWidth = table.getColumnModel().getColumnMargin();

      Color c = g.getColor();
      g.setColor( table.getGridColor() );
      g.drawRect( cellRect.x, cellRect.y, cellRect.width - 1, cellRect.height - 1 );
      g.setColor( c );

      cellRect.setBounds( cellRect.x + spacingWidth / 2, cellRect.y + spacingHeight / 2,
            cellRect.width - spacingWidth, cellRect.height - spacingHeight );

      if (table.isEditing() && table.getEditingRow() == row &&
            table.getEditingColumn() == column)
      {
         Component component = table.getEditorComponent();
         component.setBounds( cellRect );
         component.validate();
      } else
      {
         TableCellRenderer renderer;
         if (table.getModel().getRowCount()>row && separatorClass.isInstance( ((EventTableModel) table.getModel()).getElementAt( row ) ))
            renderer = table.getDefaultRenderer( separatorClass );
         else
            renderer = table.getCellRenderer( row, column );

         Component component = table.prepareRenderer( renderer, row, column );

         if (component.getParent() == null)
         {
            rendererPane.add( component );
         }

         rendererPane.paintComponent( g, component, table, cellRect.x, cellRect.y,
               cellRect.width, cellRect.height, true );
      }

   }
}
