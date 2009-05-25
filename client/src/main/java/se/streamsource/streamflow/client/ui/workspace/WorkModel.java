/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.workspace;

import javax.swing.table.DefaultTableModel;
import java.util.Date;

/**
 * JAVADOC
 */
public class WorkModel
        extends DefaultTableModel
{
    public WorkModel()
    {
        addColumn("name");
        addColumn("date");
        addColumn("completed");

        addRow(new Object[]{true, "Handling 1", new Date()});
        addRow(new Object[]{false, "Handling 2", new Date()});
        addRow(new Object[]{false, "Handling 3", new Date()});
        addRow(new Object[]{false, "Handling 4", new Date()});
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 0)
            return Boolean.class;
        else
            return super.getColumnClass(columnIndex);
    }
}
