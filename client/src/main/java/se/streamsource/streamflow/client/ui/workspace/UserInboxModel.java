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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.UserInboxClientResource;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.resource.label.LabelDTO;

import java.util.Date;
import java.util.List;

/**
 * JAVADOC
 */
public class UserInboxModel
        extends AbstractTreeTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    TasksQuery query;

    InboxTaskListDTO tasks;

    String[] columnNames;
    Class[] columnClasses = {Boolean.class, String.class, Date.class};
    boolean[] columnEditable = {true, false, false};

    public UserInboxModel(@Uses UserInboxClientResource inbox)
    {
        columnNames = new String[]{"", text(description_column_header), text(created_column_header)};
        root = inbox;
    }

    @Override
    public UserInboxClientResource getRoot()
    {
        return (UserInboxClientResource) super.getRoot();
    }

    public void setInbox(UserInboxClientResource inbox) throws ResourceException
    {
        root = inbox;
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
        return node instanceof InboxTaskDTO;
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
        if (parent instanceof UserInboxClientResource)
            return tasks.tasks().get().size();
        else
            return 0;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof UserInboxClientResource)
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
        if (node instanceof InboxTaskDTO)
        {
            InboxTaskDTO task = (InboxTaskDTO) node;
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
                case 3:
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
                        InboxTaskDTO taskValue = (InboxTaskDTO) node;
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
        InboxTaskListDTO newRoot = getRoot().tasks(query).<InboxTaskListDTO>buildWith().prototype();
        boolean same = newRoot.equals(tasks);
        if (!same)
        {
            tasks = getRoot().tasks(query).<InboxTaskListDTO>buildWith().prototype();
            modelSupport.fireNewRoot();
        }
    }

    public void newTask(NewSharedTaskCommand command) throws ResourceException
    {
        getRoot().newtask(command);
        refresh();
    }

    public void removeTask(String id) throws ResourceException
    {
        getRoot().task(id).delete();
    }

    public void assignToMe(String id) throws ResourceException
    {
        getRoot().task(id).assignToMe();

        refresh();
    }

    public void markAsRead(String id) throws ResourceException
    {
        getRoot().task(id).markAsRead();
    }

    public void delegate(String taskId, String delegateeId) throws ResourceException
    {
        getRoot().task(taskId).delegate(delegateeId);
        refresh();
    }

    public void forward(String taskId, String receiverId) throws ResourceException
    {
        getRoot().task(taskId).forward(receiverId);
        refresh();
    }

    public void addLabel(String taskId, String labelId) throws ResourceException
    {
        getRoot().task(taskId).addLabel(labelId);
    }

    public void removeLabel(String taskId, String labelId) throws ResourceException
    {
        getRoot().task(taskId).removeLabel(labelId);
    }
}
