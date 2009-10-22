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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskGeneralClientResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.util.Date;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel
        implements Refreshable, EventListener

{
    @Uses
    private TaskGeneralClientResource generalClientResource;

    TaskGeneralDTO general;

    public void refresh()
    {
        try
        {
            general = (TaskGeneralDTO) generalClientResource.general().buildWith().prototype();
        } catch (Exception e)
        {
            throw new OperationException(TaskResources.could_not_refresh,  e);
        }
    }

    public TaskGeneralDTO getGeneral()
    {
        if (general == null)
            refresh();

        return general;
    }

    public void describe(String newDescription)
    {
        try
        {
            generalClientResource.describe(newDescription);
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_change_description, e);
        }
    }

    public void changeNote(String newNote)
    {
        try
        {
            generalClientResource.changeNote(newNote);
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_change_note, e);
        }
    }

    public void changeDueOn(Date newDueOn)
    {
        try
        {
            generalClientResource.changeDueOn(newDueOn);
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_change_due_on, e);
        }
    }

    public void addLabel(String label) throws ResourceException
    {
        generalClientResource.addLabel(label);
    }

    public void removeLabel(String label) throws ResourceException
    {
        generalClientResource.removeLabel(label);
    }

    public void notifyEvent( DomainEvent event )
    {

    }
}