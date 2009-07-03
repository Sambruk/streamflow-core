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

package se.streamsource.streamflow.client.resource.users.shared.projects;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import se.streamsource.streamflow.client.resource.BaseClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.assignments.SharedProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.delegations.SharedProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.inbox.SharedProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.waitingfor.SharedProjectWaitingforClientResource;

/**
 * JAVADOC
 */
public class SharedProjectClientResource
        extends BaseClientResource
{
    public SharedProjectClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public SharedProjectInboxClientResource inbox()
    {
        return getSubResource("inbox", SharedProjectInboxClientResource.class);
    }

    public SharedProjectAssignmentsClientResource assignments()
    {
        return getSubResource("assignments", SharedProjectAssignmentsClientResource.class);
    }

    public SharedProjectDelegationsClientResource delegations()
    {
        return getSubResource("delegations", SharedProjectDelegationsClientResource.class);
    }

    public SharedProjectWaitingforClientResource waitingFor()
    {
        return getSubResource("waitingfor", SharedProjectWaitingforClientResource.class);
    }

}