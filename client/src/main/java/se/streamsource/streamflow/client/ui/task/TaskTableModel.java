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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.users.workspace.TaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Base class for all models that list tasks
 */
public abstract class TaskTableModel<T extends TaskListDTO>
        extends AbstractTableModel
{
    public static final int IS_READ = 10;
    public static final int IS_DROPPED = 11;

    @Uses
    TaskListClientResource resource;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    TasksQuery query;

    protected List<? extends TaskDTO> tasks;

    protected String[] columnNames;
    protected Class[] columnClasses;
    protected boolean[] columnEditable;

    WeakModelMap<String, TaskDetailModel> taskModels = new WeakModelMap<String, TaskDetailModel>()
    {
        @Override
        protected TaskDetailModel newModel(String key)
        {
            TaskClientResource taskClientResource = getResource().task(key);
            TaskDetailModel model = obf.newObjectBuilder(TaskDetailModel.class)
                    .use(taskClientResource.general(), taskClientResource.comments(), taskClientResource.contacts()).newInstance();
            return model;
        }


    };

    public TaskListClientResource getResource()
    {
        return resource;
    }

    public void setQuery(TasksQuery query) throws ResourceException
    {
        this.query = query;
    }

    public TaskDTO getTask(int row)
    {
        return tasks.get(row);
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

    public int getRowCount()
    {
        if (tasks != null)
            return tasks.size();
        else
            return 0;
    }

    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnEditable[columnIndex];
    }

    public Object getValueAt(int rowIndex, int column)
    {
        TaskDTO task = tasks.get(rowIndex);

        if (task == null)
            return null;

        switch (column)
        {
            case 0:
                return !task.status().get().equals(TaskStates.ACTIVE);
            case 1:
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
            case 2:
                return task.creationDate().get();
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
                    Boolean completed = (Boolean) aValue;
                    if (completed)
                    {

                        TaskDTO taskValue = tasks.get(rowIndex);
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
                case 1:
                {
                    String description = (String) aValue;
                    TaskDTO taskValue = tasks.get(rowIndex);
                    if (!description.equals(taskValue.description().get()))
                    {
                        taskValue.description().set(description);
                        fireTableCellUpdated(rowIndex, column);
                    }

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
        List<? extends TaskDTO> newRoot = getResource().tasks(query);
        boolean same = newRoot.equals(tasks);
        if (!same)
        {
            tasks = newRoot;
            fireTableDataChanged();
        }
    }

    public int count()
    {
        if (tasks == null)
            return 0;
        else
        {
            int count = 0;
            for (TaskDTO taskDTO : tasks)
            {
                if (!taskDTO.isRead().get())
                {
                    count++;
                }
            }
            return count;
        }
    }

    public void createTask() throws ResourceException
    {
        getResource().createTask();
    }


    public void completeTask(int idx)
    {
        setValueAt(true, idx, 0);
    }

    public void removeTask(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).deleteCommand();
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public void assignToMe(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).assignToMe();
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public void markAsRead(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        if (!task.isRead().get())
        {
            getResource().task(task.task().get().identity()).markAsRead();
            task.isRead().set(true);
            fireTableCellUpdated(idx, 1);
        }
    }

    public void markAsUnread(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        if (task.isRead().get())
        {
            getResource().task(task.task().get().identity()).markAsUnread();
            task.isRead().set(false);
            fireTableCellUpdated(idx, 1);
        }
    }

    public void delegate(int idx, String delegateeId) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).delegate(delegateeId);
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public void forward(int idx, String receiverId) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).forward(receiverId);
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public void addLabel(int idx, ListItemValue label) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        String labelId = label.entity().get().identity();
        for (ListItemValue labelDTO : task.labels().get().items().get())
        {
            if (labelDTO.entity().get().identity().equals(labelId))
                return;
        }

        getResource().task(task.task().get().identity()).addLabel(labelId);
        task.labels().get().items().get().add(label);
        fireTableCellUpdated(idx, 1);
    }

    public void removeLabel(int idx, ListItemValue label) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        String labelId = label.entity().get().identity();
        getResource().task(task.task().get().identity()).removeLabel(labelId);
        task.labels().get().items().get().remove(label);
        fireTableCellUpdated(idx, 1);
    }

    public void dropTask(int idx) throws ResourceException
    {
        TaskDTO task = getTask(idx);

        TaskClientResource taskClientResource = getResource().task(task.task().get().identity());
        taskClientResource.drop();
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public TaskDetailModel taskDetailModel(String id)
    {
        return taskModels.get(id);
    }
}