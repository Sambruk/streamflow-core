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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforTaskClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.domain.task.TaskStates;

import java.util.Date;

/**
 * JAVADOC
 */
public class WorkspaceProjectWaitingForModel
        extends TaskTableModel
{
    public WorkspaceProjectWaitingForModel(@Uses ProjectWaitingforClientResource resource)
    {
        super(resource);
        columnNames = new String[]{"", text(description_column_header), text(delegated_to_header), text(assigned_to_header), text(delegated_on_header)};
        columnClasses = new Class[]{Boolean.class, String.class, String.class, String.class, Date.class};
        columnEditable = new boolean[]{true, false, false, false, false};
    }

    @Override
    public ProjectWaitingforClientResource getResource()
    {
        return (ProjectWaitingforClientResource) super.getResource();
    }

    @Override
    public int getColumnCount()
    {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int column)
    {
        WaitingForTaskDTO task = (WaitingForTaskDTO) tasks.get(rowIndex);
        switch (column)
        {
            case 2:
                return task.delegatedTo().get();
            case 3:
                return task.assignedTo().get();
            case 4:
                return task.delegatedOn().get();
        }

        return super.getValueAt(rowIndex, column);
    }

    public void complete(int row)
    {
        try
        {
            TaskDTO task = getTask(row);

            ProjectWaitingforTaskClientResource taskClientResource = getResource().task( task.task().get().identity() );
            if (task.status().get().equals( TaskStates.DONE))
            {
                taskClientResource.completeFinishedTask();
            } else if (task.status().get().equals(TaskStates.ACTIVE))
            {
                taskClientResource.completeWaitingForTask();
            }
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
        }
    }
}
