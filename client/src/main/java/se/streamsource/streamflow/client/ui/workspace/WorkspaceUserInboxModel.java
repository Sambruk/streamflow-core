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

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.created_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.description_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.project_column_header;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

/**
 * JAVADOC
 */
public class WorkspaceUserInboxModel
        extends TaskTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    ListValue projects;
    
    ListItemValue selectedProject;
    Map<TaskDTO, ListItemValue> selectedProjects = new HashMap<TaskDTO, ListItemValue>();
    
    public WorkspaceUserInboxModel(@Uses WorkspaceUserInboxClientResource resource)
    {
        super(resource);
        columnNames = new String[]{text(description_column_header), text(project_column_header), text(created_column_header), ""};
        columnClasses = new Class[]{String.class, JComboBox.class, Date.class, Boolean.class};
        columnEditable = new boolean[]{false, true, false, true};
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
    
    public ListValue getProjects()
    {
    	try 
    	{
    		return getResource().projects();
        } catch (ResourceException e)
        {
            throw new OperationException( WorkspaceResources.could_not_refresh_projects, e);
        }
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
            case 0:
            {
                StringBuilder desc = new StringBuilder(task.description().get());
                List<ListItemValue> labels = task.labels().get().items().get();
                if (labels.size() > 0)
                {
                    desc.append(" (");
                    String comma = "";
                    for (ListItemValue label : labels)
                    {
                        desc.append(comma + label.description().get());
                        comma = ",";
                    }
                    desc.append(")");
                }
                return desc.toString();
            }
            case 1:
            {
            	return selectedProjects.get(task);
            }
            case 2:
                return task.creationDate().get();
            case 3:
                return !task.status().get().equals(TaskStates.ACTIVE);
            case IS_READ:
                return task.isRead().get();
            case IS_DROPPED:
                return task.status().get().equals(TaskStates.DROPPED);
        }

        return null;
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
                	selectedProjects.put((TaskDTO)tasks.get(rowIndex), (ListItemValue)aValue);
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

    @Override
    public void refresh()
    {
        try
        {
            List<? extends TaskDTO> newRoot = getResource().tasks(vbf.newValue( TasksQuery.class ));
            projects = getResource().projects();
            boolean same = newRoot.equals(tasks);
            if (!same)
            {
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
}
