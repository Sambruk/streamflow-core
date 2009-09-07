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

package se.streamsource.streamflow.client.ui.overview;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsClientResource;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.created_column_header;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.description_column_header;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.resource.assignment.OverviewAssignedTaskDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class OverviewProjectAssignmentsModel
        extends TaskTableModel
{
    public OverviewProjectAssignmentsModel()
    {
        columnNames = new String[]{"", text(description_column_header), text(created_column_header), text(OverviewResources.assigned_to_column_header)};
        columnClasses = new Class[]{Boolean.class, String.class, Date.class, String.class};
        columnEditable = new boolean[]{false, false, false, false};
    }

    @Override
    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int column)
    {
        OverviewAssignedTaskDTO task = (OverviewAssignedTaskDTO) tasks.get(rowIndex);
        switch (column)
        {
            case 3:
                return task.assignedTo().get();
        }

        return super.getValueAt(rowIndex, column);
    }

    @Override
    public OverviewProjectAssignmentsClientResource getResource()
    {
        return (OverviewProjectAssignmentsClientResource) super.getResource();
    }
}