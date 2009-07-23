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
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.inbox.ProjectInboxServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MemberServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MembersServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRoleServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRolesServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RolesServerResource;
import se.streamsource.streamflow.web.resource.users.UserServerResource;
import se.streamsource.streamflow.web.resource.users.UsersServerResource;
import se.streamsource.streamflow.web.resource.users.administration.UserAdministrationServerResource;
import se.streamsource.streamflow.web.resource.users.shared.SharedServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.inbox.SharedProjectsInboxServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.inbox.SharedProjectsInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.SharedProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.waitingfor.SharedProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.waitingfor.SharedProjectWaitingForTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.delegations.SharedProjectDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.delegations.SharedProjectDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.assignments.SharedProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.projects.assignments.SharedProjectAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.SharedUserServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.assignments.SharedUserAssignedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.assignments.SharedUserAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.delegations.SharedUserDelegatedTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.delegations.SharedUserDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.inbox.SharedUserInboxServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.inbox.SharedUserInboxTaskServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.task.comments.SharedUserTaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.task.general.SharedUserTaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.waitingfor.SharedUserWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.shared.user.waitingfor.SharedUserWaitingForTaskServerResource;
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

        attach("/users/{user}/workspace", createServerResourceFinder(SharedServerResource.class));
        attach("/users/{user}/workspace/user", createServerResourceFinder(SharedUserServerResource.class));

        attach("/users/{user}/workspace/projects", createServerResourceFinder(SharedProjectsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/inbox", createServerResourceFinder(SharedProjectsInboxServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/inbox/{task}", createServerResourceFinder(SharedProjectsInboxTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/assignments", createServerResourceFinder(SharedProjectAssignmentsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/assignments/{task}", createServerResourceFinder(SharedProjectAssignedTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/delegations", createServerResourceFinder(SharedProjectDelegationsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/delegations/{task}", createServerResourceFinder(SharedProjectDelegatedTaskServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/waitingfor", createServerResourceFinder(SharedProjectWaitingForServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/waitingfor/{task}", createServerResourceFinder(SharedProjectWaitingForTaskServerResource.class));
        attach("/users/{user}/workspace/user/inbox", createServerResourceFinder(SharedUserInboxServerResource.class));
        attach("/users/{user}/workspace/user/inbox/{task}", createServerResourceFinder(SharedUserInboxTaskServerResource.class));
        attach("/users/{user}/workspace/user/assignments", createServerResourceFinder(SharedUserAssignmentsServerResource.class));
        attach("/users/{user}/workspace/user/assignments/{task}", createServerResourceFinder(SharedUserAssignedTaskServerResource.class));
        attach("/users/{user}/workspace/user/delegations", createServerResourceFinder(SharedUserDelegationsServerResource.class));
        attach("/users/{user}/workspace/user/delegations/{task}", createServerResourceFinder(SharedUserDelegatedTaskServerResource.class));
        attach("/users/{user}/workspace/user/waitingfor", createServerResourceFinder(SharedUserWaitingForServerResource.class));
        attach("/users/{user}/workspace/user/waitingfor/{task}", createServerResourceFinder(SharedUserWaitingForTaskServerResource.class));
        attach("/users/{user}/workspace/user/{view}/{task}/general", createServerResourceFinder(SharedUserTaskGeneralServerResource.class));
        attach("/users/{user}/workspace/user/{view}/{task}/comments", createServerResourceFinder(SharedUserTaskCommentsServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/{view}/{task}/general", createServerResourceFinder(SharedUserTaskGeneralServerResource.class));
        attach("/users/{user}/workspace/projects/{project}/{view}/{task}/comments", createServerResourceFinder(SharedUserTaskCommentsServerResource.class));
        attach("/users/{user}/administration", createServerResourceFinder(UserAdministrationServerResource.class));

        // OrganizationalUnits
        attach("/organizations", createServerResourceFinder(OrganizationsServerResource.class));
        attach("/organizations/{organization}", createServerResourceFinder(OrganizationServerResource.class));
        attach("/organizations/{organization}/groups", createServerResourceFinder(GroupsServerResource.class));
        attach("/organizations/{organization}/groups/{group}", createServerResourceFinder(GroupServerResource.class));
        attach("/organizations/{organization}/groups/{group}/participants", createServerResourceFinder(ParticipantsServerResource.class));
        attach("/organizations/{organization}/groups/{group}/participants/{participant}", createServerResourceFinder(ParticipantServerResource.class));
        attach("/organizations/{organization}/projects", createServerResourceFinder(ProjectsServerResource.class));
        attach("/organizations/{organization}/projects/{project}", createServerResourceFinder(ProjectServerResource.class));
        attach("/organizations/{organization}/projects/{project}/inbox", createServerResourceFinder(ProjectInboxServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members", createServerResourceFinder(MembersServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}", createServerResourceFinder(MemberServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}/roles", createServerResourceFinder(MemberRolesServerResource.class));
        attach("/organizations/{organization}/projects/{project}/members/{member}/roles/{role}", createServerResourceFinder(MemberRoleServerResource.class));
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
