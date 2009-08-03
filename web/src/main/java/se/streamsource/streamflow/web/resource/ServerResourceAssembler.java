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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.web.resource.organizations.OrganizationServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantsServerResource;
import se.streamsource.streamflow.web.resource.organizations.organizationalunits.OrganizationalUnitsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MemberServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MembersServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRoleServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.roles.MemberRolesServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RoleServerResource;
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
import se.streamsource.streamflow.web.resource.users.workspace.user.labels.UserLabelsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.comments.TaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.task.general.TaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.UserWaitingForTaskServerResource;

/**
 * Assembler for API resources
 */
public class ServerResourceAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        // Resources
        module.addObjects(
                StreamFlowServerResource.class,

                // /users
                UsersServerResource.class,
                UserServerResource.class,
                UserAdministrationServerResource.class,

                WorkspaceServerResource.class,
                WorkspaceUserServerResource.class,

                UserLabelsServerResource.class,

                UserInboxServerResource.class,
                UserInboxTaskServerResource.class,

                UserAssignmentsServerResource.class,
                UserAssignedTaskServerResource.class,

                UserDelegationsServerResource.class,
                UserDelegatedTaskServerResource.class,

                UserWaitingForServerResource.class,
                UserWaitingForTaskServerResource.class,

                TaskGeneralServerResource.class,
                TaskCommentsServerResource.class,

                WorkspaceProjectsServerResource.class,
                WorkspaceProjectServerResource.class,
                ProjectInboxServerResource.class,
                ProjectInboxTaskServerResource.class,
                ProjectAssignmentsServerResource.class,
                ProjectAssignedTaskServerResource.class,
                ProjectDelegationsServerResource.class,
                ProjectDelegatedTaskServerResource.class,
                ProjectWaitingForServerResource.class,
                ProjectWaitingForTaskServerResource.class,


                // /organizations
                OrganizationsServerResource.class,
                OrganizationServerResource.class,
                OrganizationalUnitsServerResource.class,

                GroupsServerResource.class,
                GroupServerResource.class,
                ParticipantsServerResource.class,
                ParticipantServerResource.class,

                ProjectsServerResource.class,
                ProjectServerResource.class,
                MembersServerResource.class,
                MemberServerResource.class,
                MemberRolesServerResource.class,
                MemberRoleServerResource.class,

                RolesServerResource.class,
                RoleServerResource.class
        );
    }
}
