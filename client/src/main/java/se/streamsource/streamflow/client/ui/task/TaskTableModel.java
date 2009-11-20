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
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.AbstractTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventParameters;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Base class for all models that list tasks
 */
public abstract class TaskTableModel<T extends TaskListDTO>
        extends AbstractTableModel
    implements EventListener, EventHandler, Refreshable
{
    public static final int IS_READ = 10;
    public static final int IS_DROPPED = 11;

    @Uses
    TasksModel tasksModel;

    TaskListClientResource resource;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    protected List<? extends TaskDTO> tasks;

    protected String[] columnNames;
    protected Class[] columnClasses;
    protected boolean[] columnEditable;
    private EventHandlerFilter eventFilter;
    
    protected ListValue projects;

    protected TaskTableModel( TaskListClientResource resource )
    {
        this.resource = resource;
        eventFilter = new EventHandlerFilter(this, "addedLabel", "removedLabel", "changedDescription");
   }

    public void notifyEvent( DomainEvent event )
    {
        eventFilter.handleEvent( event );
    }

    public boolean handleEvent( final DomainEvent event )
    {
        final int idx = getTaskIndex( event );

        if (idx != -1)
        {
            final TaskDTO updatedTask = getTask( idx );
            if (event.name().get().equals( "changedDescription" ))
            {
                try
                {
                    String newDesc = EventParameters.getParameter( event, "param1" );
                    updatedTask.description().set(newDesc);
                    fireTableCellUpdated( idx, 1 );
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else if (event.name().get().equals("removedLabel"))
            {
                String id = EventParameters.getParameter( event, "param1" );
                List<ListItemValue> labels = updatedTask.labels().get().items().get();
                for (ListItemValue label : labels)
                {
                    if (label.entity().get().identity().equals(id))
                    {
                        labels.remove( label );
                        fireTableCellUpdated( idx, 1 );
                        break;
                    }
                }
            } else if (event.name().get().equals( "addedLabel" ))
            {
                SwingUtilities.invokeLater(
                    new Runnable(){
                        public void run(){
                            List<ListItemValue> labels = tasksModel.models
                                    .get(updatedTask.task().get().identity())
                                    .general().getGeneral().labels().get().items().get();
                            for(ListItemValue label : labels)
                            {
                                if(label.entity().get().identity()
                                        .equals(EventParameters.getParameter(event, "param1")))
                                {
                                    List<ListItemValue> newLabels = updatedTask.labels().get().items().get();
                                    newLabels.add(label);
                                    fireTableCellUpdated(idx,1);
                                }
                            }
                        }
                    });
            }
        }
        return true;
    }

    protected TaskListClientResource getResource()
    {
        return resource;
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
                return task.creationDate().get();
            case 2:
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
                    TaskDTO taskValue = tasks.get(rowIndex);
                    if (!description.equals(taskValue.description().get()))
                    {
                        taskValue.description().set(description);
                        fireTableCellUpdated(rowIndex, column);
                    }
                    break;
                }
                case 2:
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
            }
        } catch (ResourceException e)
        {
            // TODO Better error handling
            e.printStackTrace();
        }

        return; // Skip if don't know what is going on
    }

    public void refresh()
    {
        try
        {
            List<? extends TaskDTO> newRoot = getResource().tasks(vbf.newValue( TasksQuery.class ));
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
        setValueAt(true, idx, columnNames.length-1);
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

    public void markAsRead(int idx)
    {
        try
        {
            TaskDTO task = tasks.get(idx);
            if (!task.isRead().get())
            {
                getResource().task(task.task().get().identity()).markAsRead();
                task.isRead().set(true);
                fireTableCellUpdated(idx, 1);
            }
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
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

    public void dropTask(int idx) throws ResourceException
    {
        TaskDTO task = getTask(idx);

        AbstractTaskClientResource taskClientResource = getResource().task(task.task().get().identity());
        taskClientResource.drop();
        tasks.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public TaskModel task(String id)
    {
        return tasksModel.task( id );
    }

    private int getTaskIndex( DomainEvent event )
    {
        if (tasks == null)
            return -1;

        TaskDTO updatedTask = null;
        for (int i = 0; i < tasks.size(); i++)
        {
            TaskDTO taskDTO = tasks.get( i );
            if (taskDTO.task().get().identity().equals(event.entity().get()))
            {
                return i;
            }
        }

        return -1;
    }
}