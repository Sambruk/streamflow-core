/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.streamflow.server.plugin.ContactLookup;
import se.streamsource.streamflow.web.context.surface.SurfaceContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.AccessPointContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.AccessPointsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.EndUserContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.FormDraftContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SummaryContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SubmittedFormsContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers.ProxyUserContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers.ProxyUsersContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.EndUsersContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.FormDraftsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms.RequiredFormsContext;
import se.streamsource.streamflow.web.context.surface.administration.AdministrationContext;
import se.streamsource.streamflow.web.context.caze.CaseContext;
import se.streamsource.streamflow.web.context.caze.CaseFormContext;
import se.streamsource.streamflow.web.context.caze.CaseFormsContext;
import se.streamsource.streamflow.web.context.caze.CaseGeneralContext;
import se.streamsource.streamflow.web.context.caze.CasesContext;
import se.streamsource.streamflow.web.context.caze.ContactContext;
import se.streamsource.streamflow.web.context.caze.ContactsContext;
import se.streamsource.streamflow.web.context.ServiceAvailable;
import se.streamsource.streamflow.web.context.conversation.ConversationContext;
import se.streamsource.streamflow.web.context.conversation.ConversationParticipantContext;
import se.streamsource.streamflow.web.context.conversation.ConversationParticipantsContext;
import se.streamsource.streamflow.web.context.conversation.ConversationsContext;
import se.streamsource.streamflow.web.context.conversation.MessageContext;
import se.streamsource.streamflow.web.context.conversation.MessagesContext;
import se.streamsource.streamflow.web.context.gtd.AssignmentsContext;
import se.streamsource.streamflow.web.context.gtd.InboxContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorsContext;
import se.streamsource.streamflow.web.context.organizations.CaseTypeContext;
import se.streamsource.streamflow.web.context.organizations.CaseTypesContext;
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
import se.streamsource.streamflow.web.context.organizations.SelectedCaseTypeContext;
import se.streamsource.streamflow.web.context.organizations.SelectedCaseTypesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormFieldContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormFieldsContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPageContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPagesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormContext;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormsContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabeledContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.structure.resolutions.ResolutionContext;
import se.streamsource.streamflow.web.context.structure.resolutions.ResolutionsContext;
import se.streamsource.streamflow.web.context.structure.resolutions.SelectedResolutionContext;
import se.streamsource.streamflow.web.context.structure.resolutions.SelectedResolutionsContext;
import se.streamsource.streamflow.web.context.users.ContactableContext;
import se.streamsource.streamflow.web.context.users.UserAdministrationContext;
import se.streamsource.streamflow.web.context.users.UserContext;
import se.streamsource.streamflow.web.context.users.UsersContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectAssignmentsContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewProjectsContext;
import se.streamsource.streamflow.web.context.users.workspace.DraftsContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectsContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceUserContext;
import se.streamsource.streamflow.web.infrastructure.osgi.OSGiServiceImporter;

/**
 * JAVADOC
 */
public class InteractionsAssembler
   implements Assembler
{
   public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.importServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      moduleAssembly.addObjects( InteractionConstraintsService.class );

      moduleAssembly.addObjects( RequiresPermission.RequiresPermissionConstraint.class,
            ServiceAvailable.ServiceAvailableConstraint.class);

      // Import plugins from OSGi
      moduleAssembly.importServices( ContactLookup.class ).importedBy( OSGiServiceImporter.class );

      // Only expose the root to the upper layers
      moduleAssembly.addTransients(
            RootContext.class).visibleIn( Visibility.application);

      moduleAssembly.addTransients(
            FormContext.class,
            FormFieldContext.class,
            FormFieldsContext.class,
            FormPageContext.class,
            FormPagesContext.class,
            FormsContext.class,
            SelectedFormsContext.class,
            SelectedFormContext.class,

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
            SelectedCaseTypeContext.class,
            SelectedCaseTypesContext.class,
            CaseTypeContext.class,
            CaseTypesContext.class,

            LabelableContext.class,
            LabelContext.class,
            LabeledContext.class,
            LabelsContext.class,
            SelectedLabelContext.class,
            SelectedLabelsContext.class,

            ResolutionContext.class,
            ResolutionsContext.class,
            SelectedResolutionContext.class,
            SelectedResolutionsContext.class,

            ContactableContext.class,
            UsersContext.class,
            UserContext.class,

            OverviewContext.class,
            OverviewProjectAssignmentsContext.class,
            OverviewProjectContext.class,
            OverviewProjectsContext.class,

            WorkspaceContext.class,
            WorkspaceProjectContext.class,
            WorkspaceProjectsContext.class,
            UserAdministrationContext.class,
            WorkspaceUserContext.class,

            DraftsContext.class,
            InboxContext.class,
            AssignmentsContext.class,

            ContactContext.class,
            ContactsContext.class,
            CaseContext.class,
            CaseFormsContext.class,
            CaseFormContext.class,
            CaseGeneralContext.class,
            CasesContext.class,

            ConversationContext.class,
            ConversationParticipantContext.class,
            ConversationParticipantsContext.class,
            ConversationsContext.class,
            MessageContext.class,
            MessagesContext.class,

            // Surface
            SurfaceContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.OrganizationContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.projects.ProjectsContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.projects.CaseTypesContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.projects.LabelsContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.OrganizationsContext.class,
            se.streamsource.streamflow.web.context.surface.accesspoints.endusers.CaseContext.class,
            EndUserContext.class,
            EndUsersContext.class,
            SubmittedFormsContext.class,
            RequiredFormsContext.class,
            FormDraftsContext.class,
            FormDraftContext.class,
            SummaryContext.class,
            AdministrationContext.class,
            AccessPointsContext.class,
            AccessPointContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints.AccessPointContext.class,
            se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints.AccessPointsContext.class,
            ProxyUserContext.class,
            ProxyUsersContext.class
            );
   }
}
