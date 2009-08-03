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
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.users.workspace.TaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.label.LabelDTO;
import se.streamsource.streamflow.resource.task.NewTaskCommand;
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

    @Uses TaskListClientResource resource;

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
            TaskDetailModel model = obf.newObjectBuilder(TaskDetailModel.class).use(taskClientResource.general(), taskClientResource.comments()).newInstance();
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
        switch (column)
        {
            case 0:
                return !task.status().get().equals(TaskStates.ACTIVE);
            case 1:
            {
                String desc = task.description().get();
                List<LabelDTO> labels = task.labels().get().labels().get();
                if (labels.size() > 0)
                {
                    desc+= " (";
                    String comma = "";
                    for (LabelDTO label : labels)
                    {
                        desc+=comma+label.description().get();
                        comma=",";
                    }
                    desc+=")";
                }
                return desc;
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
                        EntityReference task = taskValue.task().get();
                        getResource().task(task.identity()).complete();

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
        List<? extends TaskDTO> newRoot = getResource().tasks(query);
        boolean same = newRoot.equals(tasks);
        if (!same)
        {
            tasks = newRoot;
            fireTableRowsUpdated(0, tasks.size()-1);
        }
    }

    public int unreadCount()
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

    public void newTask(NewTaskCommand command) throws ResourceException
    {
        getResource().newtask(command);
        refresh();
    }

    public void removeTask(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).delete();
        refresh();
    }

    public void assignToMe(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).assignToMe();

        refresh();
    }

    public void markAsRead(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        if (!task.isRead().get())
        {
            getResource().task(task.task().get().identity()).markAsRead();
            task.isRead().set(true);
        }
    }

    public void markAsUnread(int idx) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        if (task.isRead().get())
        {
            getResource().task(task.task().get().identity()).markAsUnread();
            task.isRead().set(false);
        }
    }

    public void delegate(int idx, String delegateeId) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).delegate(delegateeId);
        refresh();
    }

    public void forward(int idx, String receiverId) throws ResourceException
    {
        TaskDTO task = tasks.get(idx);
        getResource().task(task.task().get().identity()).forward(receiverId);
        refresh();
    }

    public void addLabel(int idx, LabelDTO label) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        String labelId = label.label().get().identity();
        for (LabelDTO labelDTO : task.labels().get().labels().get())
        {
            if (labelDTO.label().get().identity().equals(labelId))
                return;
        }

        getResource().task(task.task().get().identity()).addLabel(labelId);
        task.labels().get().labels().get().add(label);
    }

    public void removeLabel(int idx, LabelDTO label) throws ResourceException
    {
        TaskDTO task = getTask(idx);
        String labelId = label.label().get().identity();
        getResource().task(task.task().get().identity()).removeLabel(labelId);
        task.labels().get().labels().get().remove(label);
    }

    public void dropTask(int idx) throws ResourceException
    {
        TaskDTO task = getTask(idx);

        TaskClientResource taskClientResource = getResource().task(task.task().get().identity());
        taskClientResource.drop();
        refresh();
    }

    public TaskDetailModel taskDetailModel(String id)
    {
        return taskModels.get(id);
    }
}