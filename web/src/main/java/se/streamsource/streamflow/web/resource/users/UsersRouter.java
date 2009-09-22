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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import se.streamsource.streamflow.web.rest.ResourceFinder;
import se.streamsource.streamflow.web.resource.users.workspace.WorkspaceServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingForTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.WorkspaceProjectInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.WorkspaceProjectInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.WorkspaceUserServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.general.TaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.comments.TaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.contacts.TaskContactsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.contacts.TaskContactServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.UserDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.UserDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.UserAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.UserAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.UserInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.UserInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.labels.LabelsServerResource;
import se.streamsource.streamflow.web.resource.users.administration.UserAdministrationServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.assignments.OverviewProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.assignments.OverviewProjectAssignmentsTaskServerResource;
import se.streamsource.streamflow.web.resource.users.search.SearchTasksServerResource;
import se.streamsource.streamflow.web.resource.users.search.SearchTaskServerResource;

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
        attach("/workspace/user/inbox", createServerResourceFinder(UserInboxServerResource.class));
        attach("/workspace/user/inbox/{task}", createServerResourceFinder(UserInboxTaskServerResource.class));
        attach("/workspace/user/assignments", createServerResourceFinder(UserAssignmentsServerResource.class));
        attach("/workspace/user/assignments/{task}", createServerResourceFinder(UserAssignedTaskServerResource.class));
        attach("/workspace/user/delegations", createServerResourceFinder(UserDelegationsServerResource.class));
        attach("/workspace/user/delegations/{task}", createServerResourceFinder(UserDelegatedTaskServerResource.class));
        attach("/workspace/user/waitingfor", createServerResourceFinder(UserWaitingForServerResource.class));
        attach("/workspace/user/waitingfor/{task}", createServerResourceFinder(UserWaitingForTaskServerResource.class));
        attach("/workspace/user/{view}/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/workspace/user/{view}/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));
        attach("/workspace/user/{view}/{task}/contacts", createServerResourceFinder(TaskContactsServerResource.class));
        attach("/workspace/user/{view}/{task}/contacts/{index}", createServerResourceFinder(TaskContactServerResource.class));
        attach("/workspace/projects/{project}/{view}/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/workspace/projects/{project}/{view}/{task}/contacts", createServerResourceFinder(TaskContactsServerResource.class));
        attach("/workspace/projects/{project}/{view}/{task}/contacts/{index}", createServerResourceFinder(TaskContactServerResource.class));
        attach("/workspace/projects/{project}/{view}/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));
        attach("/administration", createServerResourceFinder(UserAdministrationServerResource.class));

        attach("/search", createServerResourceFinder(SearchTasksServerResource.class));
        attach("/search/{task}", createServerResourceFinder(SearchTaskServerResource.class));
        attach("/search/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/search/{task}/contacts", createServerResourceFinder(TaskContactsServerResource.class));
        attach("/search/{task}/contacts/{index}", createServerResourceFinder(TaskContactServerResource.class));
        attach("/search/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));

        // Overview
        attach("/overview/projects", createServerResourceFinder(OverviewProjectsServerResource.class));
        attach("/overview/projects/{project}", createServerResourceFinder(OverviewProjectServerResource.class));
        attach("/overview/projects/{project}/assignments", createServerResourceFinder(OverviewProjectAssignmentsServerResource.class));
        attach("/overview/projects/{project}/assignments/{task}", createServerResourceFinder(OverviewProjectAssignmentsTaskServerResource.class));
/*
        attach("/overview/projects/{project}/waitingfor", createServerResourceFinder(OverviewProjectWaitingForServerResource.class));
        attach("/overview/projects/{project}/waitingfor/{task}", createServerResourceFinder(OverviewProjectWaitingForTaskServerResource.class));
*/
        attach("/overview/projects/{project}/{view}/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/overview/projects/{project}/{view}/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));
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
