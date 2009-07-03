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
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRolesClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RolesClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.UsersClientResource;
import se.streamsource.streamflow.client.resource.users.administration.UserAdministrationClientResource;
import se.streamsource.streamflow.client.resource.users.shared.SharedClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.SharedProjectClientResource;
import se.streamsource.streamflow.client.resource.users.shared.projects.SharedProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.SharedUserClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.assignments.SharedUserAssignedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.assignments.SharedUserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.delegations.SharedUserDelegatedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.delegations.SharedUserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.SharedUserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.SharedUserInboxTaskClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.task.comments.SharedUserTaskCommentsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.task.general.SharedUserTaskGeneralClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.waitingfor.SharedUserWaitingForClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.waitingfor.SharedUserWaitingForTaskClientResource;

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
                UserClientResource.class,
                UserAdministrationClientResource.class,

                SharedClientResource.class,
                SharedUserClientResource.class,
                SharedUserInboxClientResource.class,
                SharedUserInboxTaskClientResource.class,
                SharedUserTaskGeneralClientResource.class,
                SharedUserTaskCommentsClientResource.class,

                SharedUserAssignmentsClientResource.class,
                SharedUserAssignedTaskClientResource.class,

                SharedUserDelegationsClientResource.class,
                SharedUserDelegatedTaskClientResource.class,

                SharedUserWaitingForClientResource.class,
                SharedUserWaitingForTaskClientResource.class,

                SharedProjectClientResource.class,
                SharedProjectsClientResource.class
                /*SharedProjectInboxClientResource.class,
                SharedProjectInboxTaskClientResource.class,
                SharedProjectAssignmentsClientResource.class,
                SharedProjectAssignmentsTaskClientResource.class,
                SharedProjectDelegationsClientResource.class,
                SharedProjectDelegationsTaskClientResource.class,
                SharedProjectWaitingforClientResource.class,
                SharedProjectWaitingforTaskClientResource.class*/

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

                ProjectsClientResource.class,
                ProjectClientResource.class,
                MembersClientResource.class,
                MemberClientResource.class,
                MemberRolesClientResource.class,
                MemberRoleClientResource.class,

                RolesClientResource.class,
                RoleClientResource.class).visibleIn(Visibility.application);
    }
}
