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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.WorkspaceUserDelegatedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.WorkspaceUserDelegationsClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.domain.task.TaskStates;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class WorkspaceUserDelegationsModel
        extends TaskTableModel
{
    public WorkspaceUserDelegationsModel(@Uses WorkspaceUserDelegationsClientResource resource)
    {
        super(resource);
        columnNames = new String[]{text(description_column_header), text(delegated_from_header), text(delegated_on_header), ""};
        columnClasses = new Class[]{String.class, String.class, Date.class, Boolean.class};
        columnEditable = new boolean[]{false, false, false, true};
    }

    @Override
    public WorkspaceUserDelegationsClientResource getResource()
    {
        return (WorkspaceUserDelegationsClientResource) super.getResource();
    }

    @Override
    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int column)
    {
        DelegatedTaskDTO task = (DelegatedTaskDTO) tasks.get(rowIndex);
        switch (column)
        {
            case 1:
                return task.delegatedFrom().get();
            case 2:
                return task.delegatedOn().get();
            case 3:
                return !task.status().get().equals(TaskStates.ACTIVE);
        }

        return super.getValueAt(rowIndex, column);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int column)
    {
        try
        {
            switch (column)
            {
                case 0:
                {
                    String description = (String) aValue;
                    TaskDTO taskValue = (TaskDTO) tasks.get(rowIndex);
                    if (!description.equals(taskValue.description().get()))
                    {
                        taskValue.description().set(description);
                        fireTableCellUpdated(rowIndex, column);
                    }
                    break;
                }
                case 3:
                {
                    Boolean completed = (Boolean) aValue;
                    if (completed)
                    {

                        TaskDTO taskValue = (TaskDTO) tasks.get(rowIndex);
                        if (taskValue.status().get() == TaskStates.ACTIVE)
                        {
                            EntityReference task = taskValue.task().get();
                            getResource().task(task.identity()).complete();

                            taskValue.status().set(TaskStates.COMPLETED);
                            fireTableCellUpdated(rowIndex, column);
                        }
                    }
                    break;
                }
            }
        } catch (ResourceException e)
        {
            // TODO Better error handling
            e.printStackTrace();
        }

        return; // Skip if don't know what is going on
    }

    public void reject(int idx) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        WorkspaceUserDelegatedTaskClientResource resource = (WorkspaceUserDelegatedTaskClientResource) getResource().task(task.task().get().identity());
        resource.reject();
    }
}
