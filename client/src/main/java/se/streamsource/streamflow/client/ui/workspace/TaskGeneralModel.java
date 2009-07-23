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

import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.shared.user.task.general.UserTaskGeneralClientResource;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.io.IOException;
import java.util.Observable;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel
        extends Observable
        implements Refreshable

{
    private UserTaskGeneralClientResource generalClientResource;

    TaskGeneralDTO general;

    public void refresh() throws IOException, ResourceException
    {
        if (generalClientResource != null)
        {
            general = generalClientResource.general();
            setChanged();
            super.notifyObservers();
        }
    }

    public TaskGeneralDTO getGeneral()
    {
        return general;
    }

    public void setResource(UserTaskGeneralClientResource generalClientResource) throws IOException, ResourceException
    {
        this.generalClientResource = generalClientResource;
    }

    public void updateGeneral(TaskGeneralDTO taskGeneralDTO) throws ResourceException
    {
        generalClientResource.put(new StringRepresentation(taskGeneralDTO.toJSON(), MediaType.APPLICATION_JSON));
    }
}