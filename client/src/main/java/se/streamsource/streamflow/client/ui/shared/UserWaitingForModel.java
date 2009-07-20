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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.users.shared.user.waitingfor.SharedUserWaitingForClientResource;
import static se.streamsource.streamflow.client.ui.shared.WorkspaceResources.*;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class UserWaitingForModel
        extends AbstractTreeTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    WaitingForTaskListDTO tasks;

    String[] columnNames = {"", text(description_column_header), text(delegated_to_header), text(assigned_to_header), text(delegated_on_header)};
    Class[] columnClasses = {Boolean.class, String.class, String.class, String.class, Date.class};
    boolean[] columnEditable = {true, false, false, false, false};

    @Override
    public SharedUserWaitingForClientResource getRoot()
    {
        return (SharedUserWaitingForClientResource) super.getRoot();
    }

    public void setWaitingFor(SharedUserWaitingForClientResource waitingForClientResource) throws ResourceException
    {
        root = waitingForClientResource;
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
        return node instanceof WaitingForTaskDTO;
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
        if (parent instanceof SharedUserWaitingForClientResource)
            return tasks.tasks().get().size();
        else
            return 0;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof SharedUserWaitingForClientResource)
            return tasks.tasks().get().indexOf(child);
        else
            return -1;
    }

    public int getColumnCount()
    {
        return 5;
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return columnEditable[i];
    }

    public Object getValueAt(Object node, int column)
    {
        if (node instanceof WaitingForTaskDTO)
        {
            WaitingForTaskDTO task = (WaitingForTaskDTO) node;
            switch (column)
            {
                case 0:
                    return !task.status().get().equals(TaskStates.ACTIVE);
                case 1:
                    return task.description().get();
                case 2:
                    return task.delegatedTo().get();
                case 3:
                    return task.assignedTo().get();
                case 4:
                    return task.delegatedOn().get();
                case 5:
                    return task.isRead().get();
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
                        WaitingForTaskDTO taskValue = (WaitingForTaskDTO) node;
                        EntityReference task = taskValue.task().get();
                        getRoot().task(task.identity()).complete();

                        taskValue.status().set(TaskStates.COMPLETED);
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

    public void refresh() throws ResourceException
    {
        tasks = getRoot().tasks().<WaitingForTaskListDTO>buildWith().prototype();
        modelSupport.fireNewRoot();
    }

    public void delegate(String task, String delegatee) throws ResourceException
    {
        getRoot().task(task).delegate(delegatee);
    }

    public void markAsRead(String id) throws ResourceException
    {
        getRoot().task(id).markAsRead();
    }
}