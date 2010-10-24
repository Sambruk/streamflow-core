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

package se.streamsource.streamflow.web.assembler;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.DefaultResponseWriterFactory;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.ServiceAvailable;
import se.streamsource.streamflow.web.context.account.AccountContext;
import se.streamsource.streamflow.web.context.account.ContactableContext;
import se.streamsource.streamflow.web.context.account.ProfileContext;
import se.streamsource.streamflow.web.context.administration.AdministrationContext;
import se.streamsource.streamflow.web.context.administration.ResolutionContext;
import se.streamsource.streamflow.web.context.administration.ResolutionsContext;
import se.streamsource.streamflow.web.context.administration.SelectedResolutionContext;
import se.streamsource.streamflow.web.context.administration.SelectedResolutionsContext;
import se.streamsource.streamflow.web.context.administration.UserContext;
import se.streamsource.streamflow.web.context.administration.UsersContext;
import se.streamsource.streamflow.web.context.cases.CaseFormDraftContext;
import se.streamsource.streamflow.web.context.cases.CasePossibleFormContext;
import se.streamsource.streamflow.web.context.cases.CasePossibleFormsContext;
import se.streamsource.streamflow.web.context.conversation.ConversationContext;
import se.streamsource.streamflow.web.context.conversation.ConversationParticipantContext;
import se.streamsource.streamflow.web.context.conversation.ConversationParticipantsContext;
import se.streamsource.streamflow.web.context.conversation.ConversationsContext;
import se.streamsource.streamflow.web.context.conversation.MessagesContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorContext;
import se.streamsource.streamflow.web.context.organizations.AdministratorsContext;
import se.streamsource.streamflow.web.context.organizations.CaseTypeContext;
import se.streamsource.streamflow.web.context.organizations.CaseTypesContext;
import se.streamsource.streamflow.web.context.organizations.GroupContext;
import se.streamsource.streamflow.web.context.organizations.GroupsContext;
import se.streamsource.streamflow.web.context.organizations.MemberContext;
import se.streamsource.streamflow.web.context.organizations.MembersContext;
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
import se.streamsource.streamflow.web.context.organizations.SelectedCaseTypeContext;
import se.streamsource.streamflow.web.context.organizations.SelectedCaseTypesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormFieldContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormInfoContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPageContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormPagesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormSignatureContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormSignaturesContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormContext;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormsContext;
import se.streamsource.streamflow.web.context.overview.OverviewContext;
import se.streamsource.streamflow.web.context.overview.OverviewProjectAssignmentsContext;
import se.streamsource.streamflow.web.context.overview.OverviewProjectsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.SelectedTemplateContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.AccessPointContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.AccessPointsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.EndUserContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.EndUsersContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.SurfaceCaseContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.SurfaceFormDraftContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.SurfaceFormDraftsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SurfaceSummaryContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms.SurfaceRequiredFormsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormsContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints.AccessPointAdministrationContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints.AccessPointsAdministrationContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers.ProxyUserContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers.ProxyUsersContext;
import se.streamsource.streamflow.web.context.users.workspace.DraftsContext;
import se.streamsource.streamflow.web.context.users.workspace.SavedSearchContext;
import se.streamsource.streamflow.web.context.users.workspace.SavedSearchesContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectsContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseActionsContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseGeneralContext;
import se.streamsource.streamflow.web.context.workspace.cases.LabelableContext;
import se.streamsource.streamflow.web.context.workspace.cases.LabeledContext;
import se.streamsource.streamflow.web.context.workspace.cases.attachment.AttachmentContext;
import se.streamsource.streamflow.web.context.workspace.cases.attachment.AttachmentsContext;
import se.streamsource.streamflow.web.context.workspace.cases.contact.ContactContext;
import se.streamsource.streamflow.web.context.workspace.cases.contact.ContactsContext;
import se.streamsource.streamflow.web.context.workspace.cases.form.CaseSubmittedFormsContext;
import se.streamsource.streamflow.web.context.workspace.context.AssignmentsContext;
import se.streamsource.streamflow.web.context.workspace.context.InboxContext;
import se.streamsource.streamflow.web.context.workspace.search.WorkspaceSearchContext;
import se.streamsource.streamflow.web.resource.RootResource;
import se.streamsource.streamflow.web.resource.account.AccountResource;
import se.streamsource.streamflow.web.resource.administration.AdministrationResource;
import se.streamsource.streamflow.web.resource.administration.ProxyUsersResource;
import se.streamsource.streamflow.web.resource.administration.ResolutionsResource;
import se.streamsource.streamflow.web.resource.administration.ServerResource;
import se.streamsource.streamflow.web.resource.administration.UserResource;
import se.streamsource.streamflow.web.resource.administration.UsersResource;
import se.streamsource.streamflow.web.resource.cases.CaseFormDraftsResource;
import se.streamsource.streamflow.web.resource.conversation.ConversationParticipantsResource;
import se.streamsource.streamflow.web.resource.conversation.ConversationResource;
import se.streamsource.streamflow.web.resource.conversation.ConversationsResource;
import se.streamsource.streamflow.web.resource.organizations.AdministratorsResource;
import se.streamsource.streamflow.web.resource.organizations.CaseTypeResource;
import se.streamsource.streamflow.web.resource.organizations.CaseTypesResource;
import se.streamsource.streamflow.web.resource.organizations.GroupResource;
import se.streamsource.streamflow.web.resource.organizations.GroupsResource;
import se.streamsource.streamflow.web.resource.organizations.MembersResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationUsersResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationalUnitResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationalUnitsResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsResource;
import se.streamsource.streamflow.web.resource.organizations.ParticipantsResource;
import se.streamsource.streamflow.web.resource.organizations.ProjectResource;
import se.streamsource.streamflow.web.resource.organizations.ProjectsResource;
import se.streamsource.streamflow.web.resource.organizations.RolesResource;
import se.streamsource.streamflow.web.resource.organizations.SelectedCaseTypesResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormFieldResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormPageResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormPagesResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormSignaturesResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormsResource;
import se.streamsource.streamflow.web.resource.organizations.forms.SelectedFormsResource;
import se.streamsource.streamflow.web.resource.overview.OverviewProjectResource;
import se.streamsource.streamflow.web.resource.overview.OverviewProjectsResource;
import se.streamsource.streamflow.web.resource.overview.OverviewResource;
import se.streamsource.streamflow.web.resource.structure.SelectedTemplateResource;
import se.streamsource.streamflow.web.resource.structure.labels.LabelResource;
import se.streamsource.streamflow.web.resource.structure.labels.LabelableResource;
import se.streamsource.streamflow.web.resource.structure.labels.LabelsResource;
import se.streamsource.streamflow.web.resource.structure.labels.SelectedLabelsResource;
import se.streamsource.streamflow.web.resource.structure.resolutions.SelectedResolutionsResource;
import se.streamsource.streamflow.web.resource.surface.SurfaceResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.AccessPointResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.AccessPointsResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.EndUserResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.EndUsersResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.SurfaceCaseResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.formdrafts.SurfaceFormDraftResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.formdrafts.SurfaceFormDraftsResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormsResource;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints.AccessPointAdministrationResource;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints.AccessPointsAdministrationResource;
import se.streamsource.streamflow.web.resource.workspace.WorkspaceResource;
import se.streamsource.streamflow.web.resource.workspace.cases.AttachmentsResource;
import se.streamsource.streamflow.web.resource.workspace.cases.CaseGeneralResource;
import se.streamsource.streamflow.web.resource.workspace.cases.CasePossibleFormsResource;
import se.streamsource.streamflow.web.resource.workspace.cases.CaseResource;
import se.streamsource.streamflow.web.resource.workspace.cases.ContactsResource;
import se.streamsource.streamflow.web.resource.workspace.cases.WorkspaceCasesResource;
import se.streamsource.streamflow.web.resource.workspace.context.WorkspaceContextResource;
import se.streamsource.streamflow.web.resource.workspace.context.WorkspaceDraftsResource;
import se.streamsource.streamflow.web.resource.workspace.context.WorkspaceProjectResource;
import se.streamsource.streamflow.web.resource.workspace.context.WorkspaceProjectsResource;
import se.streamsource.streamflow.web.resource.workspace.search.SavedSearchesResource;
import se.streamsource.streamflow.web.resource.workspace.search.WorkspaceSearchResource;
import se.streamsource.streamflow.web.rest.StreamflowRestlet;
import se.streamsource.streamflow.web.rest.StreamflowResultConverter;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class ContextAssembler
{
   public void assemble( LayerAssembly layer )
         throws AssemblyException
   {
      interactions( layer.moduleAssembly( "Context" ) );
   }

   private void interactions( ModuleAssembly module ) throws AssemblyException
   {
      module.importServices( ResultConverter.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );

      module.addObjects( StreamflowResultConverter.class );

      module.addObjects( DefaultResponseWriterFactory.class );
      new DCIAssembler().assemble( module );

      module.importServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      module.addObjects( InteractionConstraintsService.class );

      module.addObjects( RequiresPermission.RequiresPermissionConstraint.class,
            ServiceAvailable.ServiceAvailableConstraint.class );

      // Import file handling service for file uploads
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold( 1024 * 1000 * 30 ); // 30 Mb threshold TODO Make this into real service and make this number configurable
      module.importServices( FileItemFactory.class ).importedBy( INSTANCE ).setMetaInfo( factory );

      module.addObjects( StreamflowRestlet.class ).visibleIn( Visibility.application );

      addResourceContexts( module,
            RootResource.class,

            DescribableContext.class,

            // Account
            AccountContext.class,
            ProfileContext.class,
            ContactableContext.class,
            AccountResource.class,

            // Administration
            AdministrationContext.class,
            AdministrationResource.class,

            ServerResource.class,
            UsersContext.class,
            UsersResource.class,
            UserContext.class,
            UserResource.class,


            OrganizationsResource.class,
            OrganizationResource.class,

            AdministratorContext.class,
            AdministratorsContext.class,
            AdministratorsResource.class,

            LabelContext.class,
            LabelResource.class,
            LabelsContext.class,
            LabelsResource.class,


            FormContext.class,
            FormResource.class,

            FormInfoContext.class,
            FormPageContext.class,
            FormPageResource.class,
            FormPagesContext.class,
            FormPagesResource.class,
            FormFieldContext.class,
            FormFieldResource.class,
            FormSignaturesContext.class,
            FormSignaturesResource.class,
            FormSignatureContext.class,
            FormsContext.class,
            FormsResource.class,
            SelectedFormsContext.class,
            SelectedFormsResource.class,
            SelectedFormContext.class,

            GroupContext.class,
            GroupResource.class,
            GroupsContext.class,
            GroupsResource.class,
            MemberContext.class,
            MembersContext.class,
            MembersResource.class,
            OrganizationalUnitContext.class,
            OrganizationalUnitResource.class,
            OrganizationalUnitsContext.class,
            OrganizationalUnitsResource.class,
            OrganizationUserContext.class,
            OrganizationUsersContext.class,
            OrganizationUsersResource.class,
            ParticipantContext.class,
            ParticipantsContext.class,
            ParticipantsResource.class,
            ProjectContext.class,
            ProjectResource.class,
            ProjectsContext.class,
            ProjectsResource.class,
            RoleContext.class,
            RoleContext.class,
            RolesResource.class,
            SelectedCaseTypeContext.class,
            SelectedCaseTypesContext.class,
            SelectedCaseTypesResource.class,
            CaseTypeContext.class,
            CaseTypeResource.class,
            CaseTypesContext.class,
            CaseTypesResource.class,

            LabelableContext.class,
            LabelableResource.class,
            LabeledContext.class,
            SelectedLabelContext.class,
            SelectedLabelsContext.class,
            SelectedLabelsResource.class,

            ResolutionContext.class,
            ResolutionsContext.class,
            ResolutionsResource.class,
            SelectedResolutionContext.class,
            SelectedResolutionsContext.class,
            SelectedResolutionsResource.class,

            // Overview
            OverviewContext.class,
            OverviewResource.class,
            OverviewProjectAssignmentsContext.class,
            OverviewProjectResource.class,
            OverviewProjectsContext.class,
            OverviewProjectsResource.class,

            // Workspace
            WorkspaceResource.class,

            WorkspaceContext.class,
            WorkspaceContextResource.class,
            WorkspaceDraftsResource.class,
            WorkspaceProjectsContext.class,
            WorkspaceProjectsResource.class,
            WorkspaceProjectResource.class,

            WorkspaceSearchContext.class,
            WorkspaceSearchResource.class,
            SavedSearchesContext.class,
            SavedSearchesResource.class,
            SavedSearchContext.class,

            DraftsContext.class,
            InboxContext.class,
            AssignmentsContext.class,

            ContactContext.class,
            ContactsContext.class,
            ContactsResource.class,
            CaseContext.class,
            CaseActionsContext.class,
            CaseResource.class,
            CaseSubmittedFormsContext.class,
            CaseFormDraftsResource.class,
            CaseFormDraftContext.class,
            CasePossibleFormsContext.class,
            CasePossibleFormsResource.class,
            CasePossibleFormContext.class,
            CaseGeneralContext.class,
            CaseGeneralResource.class,
            WorkspaceCasesResource.class,

            ConversationContext.class,
            ConversationResource.class,
            ConversationParticipantContext.class,
            ConversationParticipantsContext.class,
            ConversationParticipantsResource.class,
            ConversationsContext.class,
            ConversationsResource.class,
            MessagesContext.class,

            AttachmentsContext.class,
            AttachmentsResource.class,
            AttachmentContext.class,
            SelectedTemplateResource.class,
            SelectedTemplateContext.class,

            // Surface
            SurfaceResource.class,
            SurfaceCaseContext.class,
            SurfaceCaseResource.class,
            EndUserContext.class,
            EndUserResource.class,
            EndUsersContext.class,
            EndUsersResource.class,
            SurfaceSubmittedFormsContext.class,
            SurfaceSubmittedFormsResource.class,
            SurfaceSubmittedFormContext.class,
            SurfaceRequiredFormsContext.class,
            SurfaceFormDraftsContext.class,
            SurfaceFormDraftsResource.class,
            SurfaceFormDraftContext.class,
            SurfaceFormDraftResource.class,
            SurfaceSummaryContext.class,
            AccessPointsContext.class,
            AccessPointsResource.class,
            AccessPointContext.class,
            AccessPointResource.class,
            AccessPointAdministrationContext.class,
            AccessPointAdministrationResource.class,
            AccessPointsAdministrationContext.class,
            AccessPointsAdministrationResource.class,
            ProxyUserContext.class,
            ProxyUsersContext.class,
            ProxyUsersResource.class
      );
   }

   private void addResourceContexts( ModuleAssembly module, Class<?>... resourceContextClasses ) throws AssemblyException
   {
      for (Class<?> resourceContextClass : resourceContextClasses)
      {
         if (CommandQueryResource.class.isAssignableFrom( resourceContextClass ))
         {
            module.addObjects( resourceContextClass );
         } else if (TransientComposite.class.isAssignableFrom( resourceContextClass ))
         {
            module.addTransients( (Class<TransientComposite>) resourceContextClass );
         } else
         {
            module.addObjects( resourceContextClass );
         }
      }
   }
}