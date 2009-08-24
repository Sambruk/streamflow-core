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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskContactsClientResource;
import se.streamsource.streamflow.resource.task.TaskContactDTO;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;

import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * List of contacts for a task
 */
public class TaskContactsModel
        extends AbstractListModel
        implements Refreshable

{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    private TaskContactsClientResource contactsClientResource;

    List<TaskContactDTO> contacts = Collections.emptyList();

    public void refresh() throws IOException, ResourceException
    {
        TaskContactsDTO contactsDTO = (TaskContactsDTO) contactsClientResource.contacts().buildWith().prototype();
        contacts = contactsDTO.contacts().get();
        fireContentsChanged(this, 0, getSize());
    }

    public List<TaskContactDTO> getContacts()
    {
        return contacts;
    }

    public TaskContactsClientResource getTaskContactsClientResource()
    {
        return contactsClientResource;
    }

    public int getSize()
    {
        return contacts.size();
    }

    public Object getElementAt(int i)
    {
        return contacts.get(i);
    }

    public void addContact()
    {
        try
        {
            contactsClientResource.add();
            refresh();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    public void updateElement(TaskContactDTO contact, int selectedIndex)
    {
        try
        {
            contactsClientResource.taskContact(selectedIndex).update(contact);
            contacts.add(selectedIndex, contact);
            contacts.remove(selectedIndex+1);
            fireContentsChanged(this, selectedIndex, selectedIndex);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    public void removeElement(int selectedIndex)
    {
        try
        {
            contactsClientResource.taskContact(selectedIndex).delete();
            contacts.remove(selectedIndex);
            fireContentsChanged(this,0,getSize());
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }
}