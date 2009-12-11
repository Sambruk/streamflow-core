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

package se.streamsource.streamflow.client.resource.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.io.IOException;
import java.util.Date;

/**
 * Mapped to /task/{id}/general
 */
public class TaskGeneralClientResource
        extends CommandQueryClientResource
{
    public TaskGeneralClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public TaskGeneralDTO general() throws IOException, ResourceException
    {
        return query("general", TaskGeneralDTO.class);
    }

    public void changeDescription(String newDescription) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(newDescription);
        putCommand("changedescription", builder.newInstance());
    }

    public void changeNote(String newNote) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(newNote);
        putCommand("changenote", builder.newInstance());
    }

    public void changeDueOn(Date newDueOn) throws ResourceException
    {
        ValueBuilder<DateDTO> builder = vbf.newValueBuilder(DateDTO.class);
        builder.prototype().date().set(newDueOn);
        putCommand("changedueon", builder.newInstance());
    }

    public void addLabel(String labelId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set( EntityReference.parseEntityReference(labelId));
        postCommand("addlabel", builder.newInstance());
    }

    public void removeLabel(String labelId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(EntityReference.parseEntityReference(labelId));
        putCommand("removelabel", builder.newInstance());
    }

    public void changeTaskType(EntityReference taskTypeId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set( taskTypeId);
        postCommand("changetasktype", builder.newInstance());
    }
    
    public ListValue possibleLabels() throws ResourceException
    {
    	return query("possiblelabels", ListValue.class);
    }

    public ListValue possibleTaskTypes() throws ResourceException
    {
    	return query("possibletasktypes", ListValue.class);
    }
}