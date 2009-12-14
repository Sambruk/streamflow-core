/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.projects.members;

import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractTableSelectionModel<T>
      extends AbstractTableModel
{
   Class[] columnClasses = {Boolean.class, String.class};
   boolean[] columnEditable = {true, false};
   String[] columnNames;

   abstract public T getSelected();

   public void setColumnNames( String... columnNames )
   {
      this.columnNames = columnNames;
   }

   public AbstractTableSelectionModel()
   {
      clearSelection();
   }

   public ListValue getModel()
   {
      return model;
   }

   private ListValue model;

   public void setModel( ListValue projects )
   {
      this.model = projects;
      clearSelection();
   }

   abstract public void clearSelection();

   public Class<?> getColumnClass( int column )
   {
      return columnClasses[column];
   }

   public boolean isCellEditable( int row, int column )
   {
      return columnEditable[column];
   }

   public String getColumnName( int column )
   {
      if (columnNames == null) return "";
      return columnNames[column];
   }

   public int getRowCount()
   {
      if (model == null) return 0;
      return model.items().get().size();
   }

   public int getColumnCount()
   {
      return columnClasses.length;
   }

}