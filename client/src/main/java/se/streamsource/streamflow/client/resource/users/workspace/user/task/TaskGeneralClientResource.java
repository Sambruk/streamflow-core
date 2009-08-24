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

package se.streamsource.streamflow.client.resource.users.workspace.user.task;

import java.io.IOException;
import java.util.Date;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

/**
 * JAVADOC
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
        return getQuery(TaskGeneralDTO.class);
    }

    public void describe(String newDescription) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(newDescription);
        putCommand("describe", builder.newInstance());
    }

    public void changeNote(String newNote) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(newNote);
        putCommand("changeNote", builder.newInstance());
    }
    
    public void changeDueOn(Date newDueOn) throws ResourceException 
    {
    	ValueBuilder<DateDTO> builder = vbf.newValueBuilder(DateDTO.class);
    	builder.prototype().date().set(newDueOn);
    	putCommand("changeDueOn", builder.newInstance());
    }
}