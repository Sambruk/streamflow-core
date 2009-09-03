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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskGeneralClientResource;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.io.IOException;
import java.util.Date;
import java.util.Observable;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel
        extends Observable
        implements Refreshable

{
    @Uses
    private TaskGeneralClientResource generalClientResource;

    TaskGeneralDTO general;

    public void refresh() throws IOException, ResourceException
    {
        general = (TaskGeneralDTO) generalClientResource.general().buildWith().prototype();
        setChanged();
        super.notifyObservers(this);
    }

    public TaskGeneralDTO getGeneral()
    {
        return general;
    }

    public void describe(String newDescription) throws ResourceException
    {
        generalClientResource.describe(newDescription);
        setChanged();
        super.notifyObservers(this);
    }

    public void changeNote(String newNote) throws ResourceException
    {
        generalClientResource.changeNote(newNote);
    }

    public void changeDueOn(Date newDueOn) throws ResourceException
    {
        generalClientResource.changeDueOn(newDueOn);
    }
}