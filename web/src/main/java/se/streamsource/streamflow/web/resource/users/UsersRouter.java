/**
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
package se.streamsource.streamflow.web.resource.users;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.streamflow.web.resource.users.administration.UserAdministrationServerResource;
import se.streamsource.streamflow.web.resource.users.overview.OverviewServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.assignments.OverviewProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.assignments.OverviewProjectAssignmentsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.search.SearchTasksServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.WorkspaceServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.WorkspaceProjectInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.WorkspaceProjectInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingForTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.WorkspaceUserServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.WorkspaceUserAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.WorkspaceUserAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.WorkspaceUserDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.WorkspaceUserDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.WorkspaceUserInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.WorkspaceUserInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.labels.LabelsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForTaskServerResource;
import se.streamsource.streamflow.web.rest.ResourceFinder;

public class UsersRouter
        extends Router
{

    @Structure
    ObjectBuilderFactory factory;

    public UsersRouter(@Uses Context context, @Structure ObjectBuilderFactory factory)
    {
        super(context);
        this.factory = factory;
        attach("", createServerResourceFinder(UserServerResource.class));
        attach("/workspace", createServerResourceFinder(WorkspaceServerResource.class));
        attach("/workspace/user", createServerResourceFinder(WorkspaceUserServerResource.class));

        attach("/workspace/projects", createServerResourceFinder(WorkspaceProjectsServerResource.class));
        attach("/workspace/projects/{project}", createServerResourceFinder(WorkspaceProjectServerResource.class));
        attach("/workspace/projects/{project}/inbox", createServerResourceFinder(WorkspaceProjectInboxServerResource.class));
        attach("/workspace/projects/{project}/inbox/{task}", createServerResourceFinder(WorkspaceProjectInboxTaskServerResource.class));
        attach("/workspace/projects/{project}/assignments", createServerResourceFinder(WorkspaceProjectAssignmentsServerResource.class));
        attach("/workspace/projects/{project}/assignments/{task}", createServerResourceFinder(WorkspaceProjectAssignmentsTaskServerResource.class));
        attach("/workspace/projects/{project}/delegations", createServerResourceFinder(WorkspaceProjectDelegationsServerResource.class));
        attach("/workspace/projects/{project}/delegations/{task}", createServerResourceFinder(WorkspaceProjectDelegationsTaskServerResource.class));
        attach("/workspace/projects/{project}/waitingfor", createServerResourceFinder(WorkspaceProjectWaitingForServerResource.class));
        attach("/workspace/projects/{project}/waitingfor/{task}", createServerResourceFinder(WorkspaceProjectWaitingForTaskServerResource.class));
        attach("/workspace/projects/{labels}/labels", createServerResourceFinder(LabelsServerResource.class));
        attach("/workspace/user/inbox", createServerResourceFinder( WorkspaceUserInboxServerResource.class));
        attach("/workspace/user/inbox/{task}", createServerResourceFinder( WorkspaceUserInboxTaskServerResource.class));
        attach("/workspace/user/assignments", createServerResourceFinder( WorkspaceUserAssignmentsServerResource.class));
        attach("/workspace/user/assignments/{task}", createServerResourceFinder( WorkspaceUserAssignedTaskServerResource.class));
        attach("/workspace/user/delegations", createServerResourceFinder( WorkspaceUserDelegationsServerResource.class));
        attach("/workspace/user/delegations/{task}", createServerResourceFinder( WorkspaceUserDelegatedTaskServerResource.class));
        attach("/workspace/user/waitingfor", createServerResourceFinder( WorkspaceUserWaitingForServerResource.class));
        attach("/workspace/user/waitingfor/{task}", createServerResourceFinder( WorkspaceUserWaitingForTaskServerResource.class));
        attach("/administration", createServerResourceFinder(UserAdministrationServerResource.class));

        attach("/search", createServerResourceFinder(SearchTasksServerResource.class));

        // Overview
        attach("/overview", createServerResourceFinder(OverviewServerResource.class));
        attach("/overview/projects", createServerResourceFinder(OverviewProjectsServerResource.class));
        attach("/overview/projects/{project}", createServerResourceFinder(OverviewProjectServerResource.class));
        attach("/overview/projects/{project}/assignments", createServerResourceFinder(OverviewProjectAssignmentsServerResource.class));
        attach("/overview/projects/{project}/assignments/{task}", createServerResourceFinder(OverviewProjectAssignmentsTaskServerResource.class));
/*
        attach("/overview/projects/{project}/waitingfor", createServerResourceFinder(OverviewProjectWaitingForServerResource.class));
        attach("/overview/projects/{project}/waitingfor/{task}", createServerResourceFinder(OverviewProjectWaitingForTaskServerResource.class));
*/
    }

    private Restlet createServerResourceFinder(Class<? extends ServerResource> resource)
    {
        ResourceFinder finder = factory.newObject(ResourceFinder.class);
        finder.setTargetClass(resource);

        Authenticator auth = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "StreamFlow");
        auth.setNext(finder);
        return auth;
    }

}
