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
import se.streamsource.streamflow.client.resource.organizations.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantsClientResource;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitClientResource;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitsClientResource;
import se.streamsource.streamflow.client.resource.organizations.policy.AdministratorClientResource;
import se.streamsource.streamflow.client.resource.organizations.policy.AdministratorsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MemberClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.roles.MemberRolesClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RoleClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RolesClientResource;
import se.streamsource.streamflow.client.resource.task.*;
import se.streamsource.streamflow.client.resource.users.UsersClientResource;
import se.streamsource.streamflow.client.resource.users.administration.UserAdministrationClientResource;
import se.streamsource.streamflow.client.resource.users.overview.OverviewClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForTaskClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.WorkspaceClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.forms.WorkspaceProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.forms.WorkspaceProjectFormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.WorkspaceProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.WorkspaceProjectInboxTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingforClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingforTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.WorkspaceUserClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.WorkspaceUserAssignedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.WorkspaceUserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.WorkspaceUserDelegatedTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.WorkspaceUserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxTaskClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForTaskClientResource;

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

                TasksClientResource.class,
                TaskClientResource.class,
                TaskGeneralClientResource.class,
                TaskCommentsClientResource.class,
                TaskContactsClientResource.class,
                TaskContactClientResource.class,
                TaskSubmittedFormsClientResource.class,
                TaskSubmittedFormClientResource.class,

                WorkspaceClientResource.class,
                WorkspaceUserClientResource.class,
                WorkspaceUserInboxClientResource.class,
                WorkspaceUserInboxTaskClientResource.class,
                
                WorkspaceUserAssignmentsClientResource.class,
                WorkspaceUserAssignedTaskClientResource.class,

                WorkspaceUserDelegationsClientResource.class,
                WorkspaceUserDelegatedTaskClientResource.class,

                WorkspaceUserWaitingForClientResource.class,
                WorkspaceUserWaitingForTaskClientResource.class,

                WorkspaceProjectClientResource.class,
                WorkspaceProjectsClientResource.class,
                WorkspaceProjectInboxClientResource.class,
                WorkspaceProjectInboxTaskClientResource.class,
                WorkspaceProjectAssignmentsClientResource.class,
                WorkspaceProjectAssignmentsTaskClientResource.class,
                WorkspaceProjectDelegationsClientResource.class,
                WorkspaceProjectDelegationsTaskClientResource.class,
                WorkspaceProjectWaitingforClientResource.class,
                WorkspaceProjectWaitingforTaskClientResource.class,
                WorkspaceProjectFormDefinitionClientResource.class,
                WorkspaceProjectFormDefinitionsClientResource.class,

                LabelsClientResource.class,
                LabelClientResource.class,
                ProjectFormDefinitionsClientResource.class,

                OverviewClientResource.class,
                OverviewProjectsClientResource.class,
                OverviewProjectClientResource.class,
                OverviewProjectAssignmentsClientResource.class,
                OverviewProjectAssignmentsTaskClientResource.class,
                OverviewProjectWaitingForClientResource.class,
                OverviewProjectWaitingForTaskClientResource.class

        ).visibleIn(Visibility.application);

        // /organizations
        module.addObjects(OrganizationClientResource.class,
                OrganizationalUnitsClientResource.class,
                OrganizationalUnitClientResource.class,
                OrganizationsClientResource.class,

                FormDefinitionsClientResource.class,

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
                RoleClientResource.class,

                AdministratorsClientResource.class,
                AdministratorClientResource.class,

                SearchClientResource.class,
                SearchTaskClientResource.class).visibleIn(Visibility.application);

        module.addObjects(EventsClientResource.class).visibleIn(Visibility.application);
    }
}
