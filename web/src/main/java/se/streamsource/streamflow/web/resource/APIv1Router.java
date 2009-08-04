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

package se.streamsource.streamflow.web.resource;

import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Context;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import se.streamsource.streamflow.web.resource.organizations.OrganizationServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantsServerResource;
import se.streamsource.streamflow.web.resource.organizations.organizationalunits.OrganizationalUnitsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.labels.LabelServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MemberServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MembersServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRoleServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRolesServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RolesServerResource;
import se.streamsource.streamflow.web.resource.users.UserServerResource;
import se.streamsource.streamflow.web.resource.users.UsersServerResource;
import se.streamsource.streamflow.web.resource.users.administration.UserAdministrationServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.WorkspaceServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.ProjectAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.ProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.ProjectDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.ProjectDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.ProjectInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.ProjectInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.ProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.ProjectWaitingForTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.WorkspaceUserServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.UserAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.UserAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.UserDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.UserDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.UserInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.UserInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.labels.LabelsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.comments.TaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.general.TaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForTaskServerResource;
import se.streamsource.streamflow.web.rest.ResourceFinder;

/**
 * Router for v1 of the StreamFlow REST API.
 */
public class APIv1Router
        extends Router
{
    private ObjectBuilderFactory factory;

    public APIv1Router(Context context, ObjectBuilderFactory factory)
    {
        super(context);
        this.factory = factory;

        attach(createServerResourceFinder(StreamFlowServerResource.class));

        // Users
        attach("/users", createServerResourceFinder(UsersServerResource.class));
        attach("/users/{user}", createServerResourceFinder(UserServerResource.class));

        attach("/users/{user}/workspace", createServerResourceFinder(WorkspaceServerResource.class));
        attach("/users/{user}/workspace/user", createServerResourceFinder(WorkspaceUserServerResource.class));

        attach("/users/{user}/workspace/projects", createServerResourceFinder(WorkspaceProjectsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}", createServerResourceFinder(WorkspaceProjectServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/inbox", createServerResourceFinder(ProjectInboxServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/inbox/{task}", createServerResourceFinder(ProjectInboxTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/assignments", createServerResourceFinder(ProjectAssignmentsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/assignments/{task}", createServerResourceFinder(ProjectAssignedTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/delegations", createServerResourceFinder(ProjectDelegationsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/delegations/{task}", createServerResourceFinder(ProjectDelegatedTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/waitingfor", createServerResourceFinder(ProjectWaitingForServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/waitingfor/{task}", createServerResourceFinder(ProjectWaitingForTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{labels}/labels", createServerResourceFinder(LabelsServerResource.class));
        attach("/users/{user}/workspace/user/inbox", createServerResourceFinder(UserInboxServerResource.class));
        attach("/users/{user}/workspace/user/inbox/{task}", createServerResourceFinder(UserInboxTaskServerResource.class));
        attach("/users/{user}/workspace/user/assignments", createServerResourceFinder(UserAssignmentsServerResource.class));
        attach("/users/{user}/workspace/user/assignments/{task}", createServerResourceFinder(UserAssignedTaskServerResource.class));
        attach("/users/{user}/workspace/user/delegations", createServerResourceFinder(UserDelegationsServerResource.class));
        attach("/users/{user}/workspace/user/delegations/{task}", createServerResourceFinder(UserDelegatedTaskServerResource.class));
        attach("/users/{user}/workspace/user/waitingfor", createServerResourceFinder(UserWaitingForServerResource.class));
        attach("/users/{user}/workspace/user/waitingfor/{task}", createServerResourceFinder(UserWaitingForTaskServerResource.class));
        attach("/users/{user}/workspace/user/{view}/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/users/{user}/workspace/user/{view}/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));
        attach("/users/{labels}/workspace/user/labels", createServerResourceFinder(LabelsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/{view}/{task}/general", createServerResourceFinder(TaskGeneralServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/{view}/{task}/comments", createServerResourceFinder(TaskCommentsServerResource.class));
        attach("/users/{user}/administration", createServerResourceFinder(UserAdministrationServerResource.class));

        // OrganizationalUnits
        attach("/organizations", createServerResourceFinder(OrganizationsServerResource.class));
        attach("/organizations/{organization}", createServerResourceFinder(OrganizationServerResource.class));
        attach("/organizations/{organization}/groups", createServerResourceFinder(GroupsServerResource.class));
        attach("/organizations/{organization}/groups/{group}", createServerResourceFinder(GroupServerResource.class));
        attach("/organizations/{organization}/groups/{group}/participants", createServerResourceFinder(ParticipantsServerResource.class));
        attach("/organizations/{organization}/groups/{group}/participants/{participant}", createServerResourceFinder(ParticipantServerResource.class));
        attach("/organizations/{organization}/projects", createServerResourceFinder(se.streamsource.streamflow.web.resource.organizations.projects.ProjectsServerResource.class));
        attach("/organizations/{organization}/projects/{project}", createServerResourceFinder(ProjectServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members", createServerResourceFinder(MembersServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}", createServerResourceFinder(MemberServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}/roles", createServerResourceFinder(MemberRolesServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}/roles/{role}", createServerResourceFinder(MemberRoleServerResource.class));
        attach("/organizations/{organization}/projects/{labels}/labels", createServerResourceFinder(LabelsServerResource.class));
        attach("/organizations/{organization}/projects/{labels}/labels/{label}", createServerResourceFinder(LabelServerResource.class));
        attach("/organizations/{organization}/roles", createServerResourceFinder(RolesServerResource.class));
        attach("/organizations/{organization}/roles/{role}", createServerResourceFinder(RolesServerResource.class));
        attach("/organizations/{organization}/organizationalunits", createServerResourceFinder(OrganizationalUnitsServerResource.class));
    }

    private Finder createServerResourceFinder(Class<? extends ServerResource> resource)
    {
        ResourceFinder finder = factory.newObject(ResourceFinder.class);
        finder.setTargetClass(resource);
        return finder;
    }

}
