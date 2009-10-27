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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskContactsClientResource;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;

import javax.swing.AbstractListModel;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * List of contacts for a task
 */
public class TaskContactsModel
        extends AbstractListModel
        implements Refreshable, EventListener, EventHandler

{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    private TaskContactsClientResource contactsClientResource;

    EventHandlerFilter eventFilter = new EventHandlerFilter(this, "contactAdded", "contactDeleted", "contactUpdated");

    public TaskContactsModel()
    {
    }

    List<ContactValue> contacts = Collections.emptyList();

    public void refresh()
    {
        try
        {
            TaskContactsDTO contactsDTO = (TaskContactsDTO) contactsClientResource.contacts().buildWith().prototype();
            contacts = contactsDTO.contacts().get();
            fireContentsChanged(this, 0, getSize());
        } catch (Exception e)
        {
            throw new OperationException(TaskResources.could_not_refresh, e);
        }
    }

    public List<ContactValue> getContacts()
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

    public void createContact()
    {
        try
        {
            contactsClientResource.add();
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_create_contact, e);
        }
    }

    public void removeElement(int selectedIndex)
    {
        try
        {
            contactsClientResource.taskContact(selectedIndex).deleteCommand();
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_remove_contact, e);
        }
    }

    public void notifyEvent( DomainEvent event )
    {
        eventFilter.handleEvent( event );
    }

    public boolean handleEvent( DomainEvent event )
    {
        if (contactsClientResource.getRequest().getResourceRef().getParentRef().getLastSegment().equals( event.entity().get()))
        {
            Logger.getLogger("workspace").info("Refresh task contacts");
            refresh();
        }

        return false;
    }
}