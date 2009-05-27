/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

/**
 * JAVADOC
 */
public class AddGroupsModel
        extends AbstractTableModel
{
    public AddGroupsModel()
    {
        clearSelection();
    }

    public void setGroups(ListValue groups)
    {
        this.groups = groups;
        clearSelection();
    }

    public void clearSelection()
    {
        selected = new HashMap<ListItemValue, Boolean>();
    }

    ListValue groups;

    public Map<ListItemValue, Boolean> getSelected()
    {
        return selected;
    }

    Map<ListItemValue, Boolean> selected;


    String[] columnNames = {"Add", "Group Name"};
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
            case 0: return selected.get(groups.items().get().get(row));
            case 1: return groups.items().get().get(row).description().get();
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int column)
    {
        if (!(Boolean) value)
        {
            selected.remove(groups.items().get().get(row));
        } else
        {
            selected.put(groups.items().get().get(row), Boolean.TRUE);
        }
    }

    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    public int getRowCount()
    {
        return groups.items().get().size();
    }

    public int getColumnCount()
    {
        return 2;
    }

}