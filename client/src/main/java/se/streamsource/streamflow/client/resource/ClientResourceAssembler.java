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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantsClientResource;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitClientResource;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRolesClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RolesClientResource;
import se.streamsource.streamflow.client.resource.users.UsersClientResource;
import se.streamsource.streamflow.client.resource.users.administration.UserAdministrationClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.WorkspaceClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.WorkspaceUserClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.UserAssignedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.UserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.UserDelegatedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.UserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.UserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.UserInboxTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.labels.ProjectLabelsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.labels.UserLabelsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskCommentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskGeneralClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.UserWaitingForClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.UserWaitingForTaskClientResource;

/**
 * JAVADOC
 */
public class ClientResourceAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addValues(ResourceListItem.class);

        // /users
        module.addObjects(StreamFlowClientResource.class,
                UsersClientResource.class,
                se.streamsource.streamflow.client.resource.users.UserClientResource.class,
                UserAdministrationClientResource.class,

                WorkspaceClientResource.class,
                WorkspaceUserClientResource.class,
                UserLabelsClientResource.class,
                UserInboxClientResource.class,
                UserInboxTaskClientResource.class,
                TaskGeneralClientResource.class,
                TaskCommentsClientResource.class,

                UserAssignmentsClientResource.class,
                UserAssignedTaskClientResource.class,

                UserDelegationsClientResource.class,
                UserDelegatedTaskClientResource.class,

                UserWaitingForClientResource.class,
                UserWaitingForTaskClientResource.class,

                WorkspaceProjectClientResource.class,
                WorkspaceProjectsClientResource.class,
                ProjectLabelsClientResource.class,
                ProjectInboxClientResource.class,
                ProjectInboxTaskClientResource.class,
                ProjectAssignmentsClientResource.class,
                ProjectAssignmentsTaskClientResource.class,
                ProjectDelegationsClientResource.class,
                ProjectDelegationsTaskClientResource.class,
                ProjectWaitingforClientResource.class,
                ProjectWaitingforTaskClientResource.class

                ).visibleIn(Visibility.application);

        // /organizations
        module.addObjects(OrganizationClientResource.class,
                OrganizationalUnitsClientResource.class,
                OrganizationalUnitClientResource.class,
                OrganizationsClientResource.class,

                GroupsClientResource.class,
                GroupClientResource.class,
                ParticipantsClientResource.class,
                ParticipantClientResource.class,

                se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource.class,
                se.streamsource.streamflow.client.resource.organizations.projects.ProjectClientResource.class,
                MembersClientResource.class,
                MemberClientResource.class,
                MemberRolesClientResource.class,
                MemberRoleClientResource.class,

                RolesClientResource.class,
                RoleClientResource.class).visibleIn(Visibility.application);
    }
}
