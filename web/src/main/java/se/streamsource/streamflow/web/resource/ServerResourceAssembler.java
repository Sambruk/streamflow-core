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

import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceSelector;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.events.EventsServerResource;
import se.streamsource.streamflow.web.resource.labels.LabelServerResource;
import se.streamsource.streamflow.web.resource.labels.LabelsServerResource;
import se.streamsource.streamflow.web.resource.labels.SelectedLabelServerResource;
import se.streamsource.streamflow.web.resource.labels.SelectedLabelsServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationCompositeResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationalUnitServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsServerResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormTemplateServerResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormTemplatesServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantsServerResource;
import se.streamsource.streamflow.web.resource.organizations.organizationalunits.OrganizationalUnitsServerResource;
import se.streamsource.streamflow.web.resource.organizations.policy.AdministratorServerResource;
import se.streamsource.streamflow.web.resource.organizations.policy.AdministratorsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MemberServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MembersServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.tasktypes.SelectedTaskTypeServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.tasktypes.SelectedTaskTypesServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RoleServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RolesServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.TaskTypeServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.TaskTypesServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.FormDefinitionServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.FormDefinitionsServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.fields.FormDefinitionFieldServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.fields.FormDefinitionFieldsServerResource;
import se.streamsource.streamflow.web.resource.task.TaskActionsServerResource;
import se.streamsource.streamflow.web.resource.task.TaskServerResource;
import se.streamsource.streamflow.web.resource.task.comments.TaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.task.contacts.TaskContactServerResource;
import se.streamsource.streamflow.web.resource.task.contacts.TaskContactsServerResource;
import se.streamsource.streamflow.web.resource.task.forms.TaskFormServerResource;
import se.streamsource.streamflow.web.resource.task.forms.TaskFormsServerResource;
import se.streamsource.streamflow.web.resource.task.general.TaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.UserAccessFilter;
import se.streamsource.streamflow.web.resource.users.UserServerResource;
import se.streamsource.streamflow.web.resource.users.UsersRouter;
import se.streamsource.streamflow.web.resource.users.UsersServerResource;
import se.streamsource.streamflow.web.resource.users.administration.UserAdministrationServerResource;
import se.streamsource.streamflow.web.resource.users.overview.OverviewServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.OverviewProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.assignments.OverviewProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.search.SearchTasksServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.WorkspaceServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.WorkspaceProjectsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.assignments.WorkspaceProjectAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.delegations.WorkspaceProjectDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.inbox.WorkspaceProjectInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor.WorkspaceProjectWaitingForServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.WorkspaceUserServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.assignments.WorkspaceUserAssignmentsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.delegations.WorkspaceUserDelegationsServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.inbox.WorkspaceUserInboxServerResource;
import se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor.WorkspaceUserWaitingForServerResource;

/**
 * Assembler for API resources
 */
public class ServerResourceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addTransients( OrganizationCompositeResource.class, GroupResource.class );

      // Resources
      module.addObjects(
            APIv1Router.class,
            UsersRouter.class,
            UserAccessFilter.class,
            StreamFlowServerResource.class,
            CompositeCommandQueryServerResource.class,

            // /users
            UsersServerResource.class,
            UserServerResource.class,
            UserAdministrationServerResource.class,

            // /users/{user}/workspace
            WorkspaceServerResource.class,
            WorkspaceUserServerResource.class,

            WorkspaceUserInboxServerResource.class,
            WorkspaceUserAssignmentsServerResource.class,
            WorkspaceUserDelegationsServerResource.class,
            WorkspaceUserWaitingForServerResource.class,

            TaskServerResource.class,
            TaskActionsServerResource.class,
            TaskGeneralServerResource.class,
            TaskCommentsServerResource.class,
            TaskContactsServerResource.class,
            TaskContactServerResource.class,
            TaskFormsServerResource.class,
            TaskFormServerResource.class,

            WorkspaceProjectsServerResource.class,
            WorkspaceProjectServerResource.class,
            WorkspaceProjectInboxServerResource.class,
            WorkspaceProjectAssignmentsServerResource.class,
            WorkspaceProjectDelegationsServerResource.class,
            WorkspaceProjectWaitingForServerResource.class,
            LabelsServerResource.class,
            LabelServerResource.class,
            SelectedLabelsServerResource.class,
            SelectedLabelServerResource.class,

            // /users/{user}/overview
            OverviewServerResource.class,
            OverviewProjectsServerResource.class,
            OverviewProjectServerResource.class,
            OverviewProjectAssignmentsServerResource.class,
            OverviewProjectWaitingForServerResource.class,

            // /organizations
            OrganizationsServerResource.class,
            OrganizationServerResource.class,
            OrganizationalUnitServerResource.class,
            OrganizationalUnitsServerResource.class,

            FormTemplateServerResource.class,
            FormTemplatesServerResource.class,

            GroupsServerResource.class,
            GroupServerResource.class,
            ParticipantsServerResource.class,
            ParticipantServerResource.class,

            ProjectsServerResource.class,
            ProjectServerResource.class,
            MembersServerResource.class,
            MemberServerResource.class,
            FormDefinitionsServerResource.class,
            FormDefinitionServerResource.class,
            FormDefinitionFieldsServerResource.class,
            FormDefinitionFieldServerResource.class,
            SelectedTaskTypesServerResource.class,
            SelectedTaskTypeServerResource.class,

            RolesServerResource.class,
            RoleServerResource.class,

            AdministratorsServerResource.class,
            AdministratorServerResource.class,

            TaskTypesServerResource.class,
            TaskTypeServerResource.class,


            // Events
            EventsServerResource.class,

            // Admin
            ConsoleServerResource.class
      );

      ModuleAssembly searchModule = module.layerAssembly().moduleAssembly( "Search" );
      searchModule.addObjects(  SearchTasksServerResource.class ).visibleIn( Visibility.layer );
      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "solrquery", "" );
      namedQueries.addQuery( queryDescriptor );

      searchModule.importServices( NamedEntityFinder.class ).
            importedBy( ServiceSelectorImporter.class ).
            setMetaInfo( ServiceSelector.withId("solr" )).
            setMetaInfo( namedQueries);
   }
}
