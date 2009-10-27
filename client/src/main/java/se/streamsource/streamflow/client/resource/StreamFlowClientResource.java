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

package se.streamsource.streamflow.client.resource;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.resource.task.TasksClientResource;
import se.streamsource.streamflow.client.resource.users.UsersClientResource;

import java.io.IOException;

/**
 * StreamFlow server resource.
 * /
 */
public class StreamFlowClientResource
        extends BaseClientResource
{
    public StreamFlowClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public String version() throws ResourceException, IOException
    {
        StreamFlowClientResource flowClientResource = getResource(getReference().clone().addSegment("static").addSegment("version.html"), StreamFlowClientResource.class);
        return flowClientResource.get().getText();
    }

    public UsersClientResource users()
    {
        return getSubResource("users", UsersClientResource.class);
    }

    public OrganizationsClientResource organizations()
    {
        return getSubResource("organizations", OrganizationsClientResource.class);
    }

    public TasksClientResource tasks()
    {
        return getSubResource("tasks", TasksClientResource.class);
    }

    public EventsClientResource events()
    {
        return getSubResource("events", EventsClientResource.class);
    }
}
