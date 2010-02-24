/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import se.streamsource.dci.context.InteractionConstraintsService;
import se.streamsource.streamflow.web.context.gtd.AssignmentsContext;
import se.streamsource.streamflow.web.context.gtd.DelegationsContext;
import se.streamsource.streamflow.web.context.gtd.InboxContext;
import se.streamsource.streamflow.web.context.gtd.WaitingForContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorsContext;
import se.streamsource.streamflow.web.context.organizations.GroupContext;
import se.streamsource.streamflow.web.context.organizations.GroupsContext;
import se.streamsource.streamflow.web.context.organizations.MemberContext;
import se.streamsource.streamflow.web.context.organizations.MembersContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationUserContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationUsersContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationalUnitContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationsContext;
import se.streamsource.streamflow.web.context.organizations.ParticipantContext;
import se.streamsource.streamflow.web.context.organizations.ParticipantsContext;
import se.streamsource.streamflow.web.context.organizations.ProjectContext;
import se.streamsource.streamflow.web.context.organizations.ProjectsContext;
import se.streamsource.streamflow.web.context.organizations.RoleContext;
import se.streamsource.streamflow.web.context.organizations.RolesContext;
import se.streamsource.streamflow.web.context.organizations.SelectedTaskTypeContext;
import se.streamsource.streamflow.web.context.organizations.SelectedTaskTypesContext;
import se.streamsource.streamflow.web.context.organizations.TaskTypeContext;
import se.streamsource.streamflow.web.context.organizations.TaskTypesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormFieldContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormFieldsContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPageContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPagesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabeledContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.task.TaskContactContext;
import se.streamsource.streamflow.web.context.task.TaskContactsContext;
import se.streamsource.streamflow.web.context.task.TaskContext;
import se.streamsource.streamflow.web.context.task.TaskConversationContext;
import se.streamsource.streamflow.web.context.task.TaskConversationsContext;
import se.streamsource.streamflow.web.context.task.TaskFormContext;
import se.streamsource.streamflow.web.context.task.TaskFormsContext;
import se.streamsource.streamflow.web.context.task.TaskGeneralContext;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.context.users.UserAdministrationContext;
import se.streamsource.streamflow.web.context.users.UserContext;
import se.streamsource.streamflow.web.context.users.UsersContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectAssignmentsContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectWaitingForContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectsContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectsContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceUserContext;

/**
 * JAVADOC
 */
public class ContextsAssembler
   implements Assembler
{
   public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.importServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      moduleAssembly.addObjects( InteractionConstraintsService.class );

      // Only expose the root the upper layers
      moduleAssembly.addTransients(
            RootContext.class).visibleIn( Visibility.application);

      moduleAssembly.addTransients(
            FormContext.class,
            FormFieldContext.class,
            FormFieldsContext.class,
            FormPageContext.class,
            FormPagesContext.class,
            FormsContext.class,

            AdministratorContext.class,
            AdministratorsContext.class,
            GroupContext.class,
            GroupsContext.class,
            MemberContext.class,
            MembersContext.class,
            OrganizationalUnitContext.class,
            OrganizationalUnitsContext.class,
            OrganizationContext.class,
            OrganizationsContext.class,
            OrganizationUserContext.class,
            OrganizationUsersContext.class,
            ParticipantContext.class,
            ParticipantsContext.class,
            ProjectContext.class,
            ProjectsContext.class,
            RoleContext.class,
            RolesContext.class,
            SelectedTaskTypeContext.class,
            SelectedTaskTypesContext.class,
            TaskTypeContext.class,
            TaskTypesContext.class,

            LabelableContext.class,
            LabelContext.class,
            LabeledContext.class,
            LabelsContext.class,
            SelectedLabelContext.class,
            SelectedLabelsContext.class,

            UsersContext.class,
            UserContext.class,

            OverviewContext.class,
            OverviewProjectAssignmentsContext.class,
            OverviewProjectContext.class,
            OverviewProjectsContext.class,
            OverviewProjectWaitingForContext.class,

            WorkspaceContext.class,
            WorkspaceProjectContext.class,
            WorkspaceProjectsContext.class,
            UserAdministrationContext.class,
            WorkspaceUserContext.class,

            InboxContext.class,
            AssignmentsContext.class,
            DelegationsContext.class,
            WaitingForContext.class,

            TaskContactContext.class,
            TaskContactsContext.class,
            TaskContext.class,
            TaskConversationContext.class,
            TaskConversationsContext.class,
            TaskFormsContext.class,
            TaskFormContext.class,
            TaskGeneralContext.class,
            TasksContext.class
            );
   }
}
