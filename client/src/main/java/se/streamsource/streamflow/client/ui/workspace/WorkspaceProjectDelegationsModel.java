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

import org.restlet.resource.ResourceException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsTaskClientResource;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class WorkspaceProjectDelegationsModel
        extends TaskTableModel
{
    public WorkspaceProjectDelegationsModel()
    {
        columnNames = new String[]{"", text(description_column_header), text(delegated_from_header), text(delegated_on_header)};
        columnClasses = new Class[]{Boolean.class, String.class, String.class, Date.class, Date.class};
        columnEditable = new boolean[]{true, false, false, false};
    }

    @Override
    public ProjectDelegationsClientResource getResource()
    {
        return (ProjectDelegationsClientResource) super.getResource();
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
            case 2:
                return task.delegatedFrom().get();
            case 3:
                return task.delegatedOn().get();
        }

        return super.getValueAt(rowIndex, column);
    }

    public void reject(int idx) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        ProjectDelegationsTaskClientResource resource = (ProjectDelegationsTaskClientResource) getResource().task(task.task().get().identity());
        resource.reject();
    }
}