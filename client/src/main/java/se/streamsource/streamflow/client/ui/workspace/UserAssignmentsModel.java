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

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.inbox.NewTaskCommand;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.shared.user.assignments.UserAssignmentsClientResource;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.created_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.description_column_header;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class UserAssignmentsModel
        extends AbstractTreeTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    TasksQuery query;

    AssignmentsTaskListDTO tasks;

    String[] columnNames = {"", text(description_column_header), text(created_column_header)};
    Class[] columnClasses = {Boolean.class, String.class, Date.class};
    boolean[] columnEditable = {true, false, false};

    @Override
    public UserAssignmentsClientResource getRoot()
    {
        return (UserAssignmentsClientResource) super.getRoot();
    }

    public void setAssignments(UserAssignmentsClientResource assignmentsClientResource) throws ResourceException
    {
        root = assignmentsClientResource;
        refresh();
    }

    public void setQuery(TasksQuery query) throws ResourceException
    {
        this.query = query;
        if (root != null)
            refresh();
    }

    @Override
    public Class<?> getColumnClass(int column)
    {
        return columnClasses[column];
    }

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return node instanceof AssignedTaskDTO;
    }

    @Override
    public int getHierarchicalColumn()
    {
        return 1;
    }

    public Object getChild(Object parent, int index)
    {
        return tasks.tasks().get().get(index);
    }

    public int getChildCount(Object parent)
    {
        if (parent instanceof UserAssignmentsClientResource)
            return tasks.tasks().get().size();
        else
            return 0;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof UserAssignmentsClientResource)
            return tasks.tasks().get().indexOf(child);
        else
            return -1;
    }

    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return columnEditable[i];
    }

    public Object getValueAt(Object node, int column)
    {
        if (node instanceof AssignedTaskDTO)
        {
            AssignedTaskDTO task = (AssignedTaskDTO) node;
            switch (column)
            {
                case 0:
                    return !task.status().get().equals(TaskStates.ACTIVE);
                case 1:
                    return task.description().get();
                case 2:
                    return task.creationDate().get();
            }
        }

        return null;
    }

    @Override
    public void setValueAt(Object value, Object node, int column)
    {
        try
        {
            switch (column)
            {
                case 0:
                {
                    Boolean completed = (Boolean) value;
                    if (completed)
                    {
                        AssignedTaskDTO taskDTO = (AssignedTaskDTO) node;
                        EntityReference task = taskDTO.task().get();
                        getRoot().task(task.identity()).complete();

                        taskDTO.status().set(TaskStates.COMPLETED);
                    }
                    break;
                }

                case 1:
                {
                    String newDescription = (String) value;
                    AssignedTaskDTO taskDTO = (AssignedTaskDTO) node;
                    EntityReference task = taskDTO.task().get();
                    ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
                    builder.prototype().description().set(newDescription);
                    getRoot().task(task.identity()).describe(builder.newInstance());

                    // Update description
                    taskDTO.description().set(newDescription);
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

    public void refresh() throws ResourceException
    {
        tasks = getRoot().tasks(query).<AssignmentsTaskListDTO>buildWith().prototype();
        modelSupport.fireNewRoot();
    }

    public void newTask(NewTaskCommand command) throws ResourceException
    {
        getRoot().newtask(command);
        modelSupport.fireNewRoot();
        tasks = null;
    }

    public void removeTask(String id) throws ResourceException
    {
        getRoot().task(id).delete();
    }
}