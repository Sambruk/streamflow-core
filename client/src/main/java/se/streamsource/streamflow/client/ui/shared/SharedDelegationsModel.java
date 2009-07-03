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
import se.streamsource.streamflow.client.resource.users.shared.user.delegations.SharedUserDelegationsClientResource;
import static se.streamsource.streamflow.client.ui.shared.SharedResources.*;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.delegation.DelegationsTaskListDTO;

import java.util.Date;

/**
 * JAVADOC
 */
public class SharedDelegationsModel
        extends AbstractTreeTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    DelegationsTaskListDTO tasks;

    String[] columnNames = {"", text(description_column_header), text(delegated_from_header), text(delegated_on_header), text(created_column_header)};
    Class[] columnClasses = {Boolean.class, String.class, String.class, Date.class};
    boolean[] columnEditable = {true, false, false, false};

    @Override
    public SharedUserDelegationsClientResource getRoot()
    {
        return (SharedUserDelegationsClientResource) super.getRoot();
    }

    public void setDelegations(SharedUserDelegationsClientResource delegationsClientResource) throws ResourceException
    {
        root = delegationsClientResource;
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
        return node instanceof DelegatedTaskDTO;
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
        if (parent instanceof SharedUserDelegationsClientResource)
            return tasks.tasks().get().size();
        else
            return 0;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof SharedUserDelegationsClientResource)
            return tasks.tasks().get().indexOf(child);
        else
            return -1;
    }

    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return columnEditable[i];
    }

    public Object getValueAt(Object node, int column)
    {
        if (node instanceof DelegatedTaskDTO)
        {
            DelegatedTaskDTO task = (DelegatedTaskDTO) node;
            switch (column)
            {
                case 0:
                    return !task.status().get().equals(TaskStates.ACTIVE);
                case 1:
                    return task.description().get();
                case 2:
                    return task.delegatedFrom().get();
                case 3:
                    return task.delegatedOn().get();
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
                        DelegatedTaskDTO taskValue = (DelegatedTaskDTO) node;
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
        tasks = getRoot().tasks().<DelegationsTaskListDTO>buildWith().prototype();
        modelSupport.fireNewRoot();
    }

    public void assignToMe(String id) throws ResourceException
    {
        getRoot().task(id).assignToMe();
        refresh();
    }

    public void reject(String id) throws ResourceException
    {
        getRoot().task(id).reject();
        refresh();
    }
}