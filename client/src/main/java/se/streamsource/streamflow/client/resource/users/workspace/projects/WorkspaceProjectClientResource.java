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

package se.streamsource.streamflow.client.resource.users.workspace.projects;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.resource.ResourceException;
import org.restlet.data.Reference;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.labels.ProjectLabelsClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;

/**
 * JAVADOC
 */
public class WorkspaceProjectClientResource
        extends CommandQueryClientResource
{
    public WorkspaceProjectClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ProjectInboxClientResource inbox()
    {
        return getSubResource("inbox", ProjectInboxClientResource.class);
    }

    public ProjectAssignmentsClientResource assignments()
    {
        return getSubResource("assignments", ProjectAssignmentsClientResource.class);
    }

    public ProjectDelegationsClientResource delegations()
    {
        return getSubResource("delegations", ProjectDelegationsClientResource.class);
    }

    public ProjectWaitingforClientResource waitingFor()
    {
        return getSubResource("waitingfor", ProjectWaitingforClientResource.class);
    }

    public ProjectLabelsClientResource labels()
    {
        return getSubResource("labels", ProjectLabelsClientResource.class);
    }

    public ListValue findUsers(String participantName) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(participantName);
        return query("findUsers", builder.newInstance(), ListValue.class);
    }

    public ListValue findGroups(String groupName) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(groupName);
        return query("findGroups", builder.newInstance(), ListValue.class);
    }

    public ListValue findProjects(String projectName) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(projectName);
        return query("findProjects", builder.newInstance(), ListValue.class);
    }
}