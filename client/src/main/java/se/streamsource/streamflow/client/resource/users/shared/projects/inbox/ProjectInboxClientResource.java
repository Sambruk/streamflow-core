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

package se.streamsource.streamflow.client.resource.users.shared.projects.inbox;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.shared.inbox.NewTaskCommand;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.UserInboxTaskClientResource;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;

/**
 * JAVADOC
 */
public class ProjectInboxClientResource
        extends CommandQueryClientResource
{
    public ProjectInboxClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public InboxTaskListDTO tasks(TasksQuery query) throws ResourceException
    {
        return query("tasks", query, InboxTaskListDTO.class);
    }

    public void newtask(NewTaskCommand command) throws ResourceException
    {
        postCommand("newtask", command);
    }

    public UserInboxTaskClientResource task(String id)
    {
        return getSubResource(id, UserInboxTaskClientResource.class);
    }
}