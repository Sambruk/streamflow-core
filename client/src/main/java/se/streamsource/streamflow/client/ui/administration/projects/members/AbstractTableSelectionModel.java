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

import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Helper class that given a model consisting of a ListValue
 * gives a selection view with a check box next to all elements
 * of the list. The method getSelected returns a set of all
 * the checked elements of the ListValue
 *
 * Subclass it and
 */
public abstract class AbstractTableSelectionModel
        extends AbstractTableModel
{

    public abstract String[] getColumnNames();

    public AbstractTableSelectionModel()
    {
        clearSelection();
    }

    private ListValue model;
    private Map<ListItemValue, Boolean> selected;

    public void setModel(ListValue projects)
    {
        this.model = projects;
        clearSelection();
        fireTableDataChanged();
    }

    public void clearSelection()
    {
        selected = new HashMap<ListItemValue, Boolean>();
    }

    public Set<ListItemValue> getSelected()
    {
        return new HashSet<ListItemValue>(selected.keySet());
    }

    Class[] columnClasses = {Boolean.class, String.class};
    boolean[] columnEditable = {true, false};



    public Class<?> getColumnClass(int column)
    {
        return columnClasses[column];
    }

    public boolean isCellEditable(int row, int column)
    {
        return columnEditable[column];
    }

    public Object getValueAt(int row, int column)
    {
        switch (column)
        {
            case 0: return selected.get(model.items().get().get(row));
            case 1: return model.items().get().get(row).description().get();
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int column)
    {
        if (!(Boolean) value)
        {
            selected.remove(model.items().get().get(row));
        } else
        {
            selected.put(model.items().get().get(row), Boolean.TRUE);
        }
    }

    public String getColumnName(int column)
    {
        return getColumnNames()[column];
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