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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
public class WorkspaceUserInboxModel
        extends TaskTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    ProjectSelectorModel2 projects;

    Map<String, ListItemValue> selectedProjects = new HashMap<String, ListItemValue>();
    
    public WorkspaceUserInboxModel(@Uses WorkspaceUserInboxClientResource resource, @Structure ObjectBuilderFactory obf)
    {
        super(resource);
        columnNames = new String[]{text(description_column_header), text(project_column_header), text(created_column_header), text(complete_task_header)};
        columnClasses = new Class[]{String.class, JComboBox.class, Date.class, Boolean.class};
        columnEditable = new boolean[]{false, true, false, false};

        projects = obf.newObjectBuilder(ProjectSelectorModel2.class)
                .use(resource).newInstance();
    }

    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public WorkspaceUserInboxClientResource getResource()
    {
        return (WorkspaceUserInboxClientResource) super.getResource();
    }

    @Override
    public void forward(int idx, String receiverId) throws ResourceException
    {
        TaskDTO task = (TaskDTO)tasks.get(idx);
        getResource().task(task.task().get().identity()).forward(receiverId);
    }


    @Override
    public Object getValueAt(int rowIndex, int column)
    {
        TaskDTO task = (TaskDTO) tasks.get(rowIndex);

        if (task == null)
            return null;

        switch (column)
        {
            case 1:
            	return selectedProjects.get(task.task().get().identity());
            case 2:
                return task.creationDate().get();
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
                case 1:
                {
                    if (aValue != null)
                    {
                        String projectName = (String) aValue;

                        ListItemValue projectId = projects.getProjectByName(projectName);

                        if (projectId != null)
                        {
                            forward(rowIndex, projectId.entity().get().identity());
                            selectedProjects.put(((TaskDTO)tasks.get(rowIndex)).task().get().identity(), projectId);
                            fireTableCellUpdated(rowIndex, column);
                        }
                    } else
                    {
                        System.out.println("Null selected");
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
            e.printStackTrace();
            throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
        }

        return; // Skip if don't know what is going on
    }

    @Override
    public void refresh()
    {
        try
        {
            List<? extends TaskDTO> newRoot = getResource().tasks(vbf.newValue( TasksQuery.class ));
            boolean same = newRoot.equals(tasks);
            if (!same)
            {
                selectedProjects.clear();

                int oldCount = tasks == null ? 0 : tasks.size();
                tasks = newRoot;

                if (newRoot.size() == oldCount)
                {
                    fireTableRowsUpdated( 0, newRoot.size() );
                } else
                {
                    fireTableDataChanged();
                }
            }
        } catch (ResourceException e)
        {
            throw new OperationException( WorkspaceResources.could_not_perform_operation, e);
        }
    }

    @Override
    public void notifyEvent( DomainEvent event )
    {
        super.notifyEvent(event);
        projects.notifyEvent(event);
    }

    public ProjectSelectorModel2 getProjectsModel()
    {
        return projects;
    }
}
