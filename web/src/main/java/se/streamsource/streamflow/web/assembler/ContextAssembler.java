/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specifications;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.api.assembler.ClientAPIAssembler;
import se.streamsource.streamflow.surface.api.assembler.SurfaceAPIAssembler;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.account.AccountContext;
import se.streamsource.streamflow.web.context.account.ContactableContext;
import se.streamsource.streamflow.web.context.account.ProfileContext;
import se.streamsource.streamflow.web.context.administration.*;
import se.streamsource.streamflow.web.context.administration.forms.*;
import se.streamsource.streamflow.web.context.administration.forms.definition.*;
import se.streamsource.streamflow.web.context.administration.labels.*;
import se.streamsource.streamflow.web.context.administration.surface.*;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.*;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.*;
import se.streamsource.streamflow.web.context.administration.surface.proxyusers.*;
import se.streamsource.streamflow.web.context.crystal.*;
import se.streamsource.streamflow.web.context.overview.*;
import se.streamsource.streamflow.web.context.structure.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.*;
import se.streamsource.streamflow.web.context.surface.endusers.*;
import se.streamsource.streamflow.web.context.workspace.*;
import se.streamsource.streamflow.web.context.workspace.cases.*;
import se.streamsource.streamflow.web.context.workspace.cases.attachment.*;
import se.streamsource.streamflow.web.context.workspace.cases.contact.*;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.*;
import se.streamsource.streamflow.web.context.workspace.cases.form.*;
import se.streamsource.streamflow.web.context.workspace.cases.general.*;
import se.streamsource.streamflow.web.infrastructure.index.*;
import se.streamsource.streamflow.web.resource.*;
import se.streamsource.streamflow.web.resource.account.*;
import se.streamsource.streamflow.web.resource.administration.*;
import se.streamsource.streamflow.web.resource.administration.surface.*;
import se.streamsource.streamflow.web.resource.crystal.*;
import se.streamsource.streamflow.web.resource.organizations.*;
import se.streamsource.streamflow.web.resource.organizations.forms.*;
import se.streamsource.streamflow.web.resource.overview.*;
import se.streamsource.streamflow.web.resource.surface.*;
import se.streamsource.streamflow.web.resource.surface.accesspoints.*;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.EndUserResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.EndUsersResource;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.*;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.formdrafts.*;
import se.streamsource.streamflow.web.resource.surface.accesspoints.endusers.submittedforms.*;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints.*;
import se.streamsource.streamflow.web.resource.surface.administration.organizations.emailaccesspoints.*;
import se.streamsource.streamflow.web.resource.surface.endusers.*;
import se.streamsource.streamflow.web.resource.workspace.*;
import se.streamsource.streamflow.web.resource.workspace.cases.*;
import se.streamsource.streamflow.web.resource.workspace.cases.conversation.*;
import se.streamsource.streamflow.web.rest.*;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class ContextAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
      interactions(layer.module("Context"));
   }

   private void interactions(ModuleAssembly module) throws AssemblyException
   {
      module.importedServices(ResultConverter.class).importedBy(ImportedServiceDeclaration.NEW_OBJECT);

      module.objects(StreamflowResultConverter.class);

      new ValueAssembler().assemble(module);
      new SurfaceAPIAssembler().assemble(module);

      module.importedServices(StreamflowCaseResultWriter.class).importedBy(ImportedServiceDeclaration.NEW_OBJECT);
      module.objects(StreamflowCaseResultWriter.class);

      new DCIAssembler().assemble(module);

      module.importedServices(InteractionConstraintsService.class).
              importedBy(NewObjectImporter.class).
              visibleIn(Visibility.application);
      module.objects(InteractionConstraintsService.class);

      module.objects(RequiresPermission.RequiresPermissionConstraint.class,
              ServiceAvailable.ServiceAvailableConstraint.class);

      // Import file handling service for file uploads
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(1024 * 1000 * 30); // 30 Mb threshold TODO Make this into real service and make this number configurable
      module.importedServices(FileItemFactory.class).importedBy(INSTANCE).setMetaInfo(factory);

      module.objects(StreamflowRestlet.class).visibleIn(Visibility.application);

      // Named queries
      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor("solrquery", "");
      namedQueries.addQuery(queryDescriptor);

      module.importedServices(NamedEntityFinder.class).
              importedBy(ServiceSelectorImporter.class).
              setMetaInfo(ServiceQualifier.withId("solr")).
              setMetaInfo(namedQueries);

      addResourceContexts(module,
              RootResource.class,

              DescribableContext.class,
              NotableContext.class,

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

              // Administration
              AdministrationContext.class,
              AdministrationResource.class,

              OrganizationsContext.class,
              OrganizationsResource.class,
              OrganizationResource.class,

              AdministratorContext.class,
              AdministratorsContext.class,
              AdministratorsResource.class,

              LabelContext.class,
              LabelResource.class,
              LabelsContext.class,
              LabelsResource.class,

              AdministratorContext.class,
              AdministratorsContext.class,
              AdministratorsResource.class,

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

              CaseAccessDefaultsContext.class,

              // Overview
              OverviewContext.class,
              OverviewResource.class,
              OverviewProjectAssignmentsContext.class,
              OverviewProjectResource.class,
              OverviewProjectsContext.class,

              // Workspace
              WorkspaceResource.class,

              WorkspaceContext.class,
              WorkspaceDraftsResource.class,
              WorkspaceProjectsContext.class,
              WorkspaceProjectsResource.class,
              WorkspaceProjectResource.class,

              PerspectivesContext.class,
              WorkspacePerspectivesResource.class,
              PerspectiveContext.class,

              DraftsContext.class,
              SearchContext.class,
              InboxContext.class,
              AssignmentsContext.class,

              ContactContext.class,
              ContactsContext.class,
              ContactsResource.class,
              CaseContext.class,
              CaseCommandsContext.class,
              CaseResource.class,
              CaseSubmittedFormsContext.class,
              CaseFormDraftResource.class,
              CaseFormDraftsResource.class,
              CaseFormDraftContext.class,
              CasePossibleFormsContext.class,
              CasePossibleFormsResource.class,
              CasePossibleFormContext.class,
              CaseGeneralContext.class,
              CaseGeneralCommandsContext.class,
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
              FormAttachmentsContext.class,
              FormAttachmentsResource.class,
              FormAttachmentContext.class,

              // Surface
              SurfaceResource.class,
              SurfaceCaseContext.class,
              SurfaceCaseResource.class,
              SurfaceDraftsResource.class,
              SurfaceDraftsContext.class,
              se.streamsource.streamflow.web.resource.surface.endusers.EndUserResource.class,
              se.streamsource.streamflow.web.resource.surface.endusers.EndUsersResource.class,
              EndUsersContext.class,
              OpenCasesResource.class,
              OpenCaseResource.class,
              OpenCasesContext.class,
              OpenCaseContext.class,
              ClosedCasesResource.class,
              ClosedCasesContext.class,
              ClosedCaseContext.class,
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
              AccessPointLabelableContext.class,
              AccessPointLabelableResource.class,
              AccessPointsAdministrationContext.class,
              AccessPointsAdministrationResource.class,
              EmailAccessPointAdministrationContext.class,
              EmailAccessPointsAdministrationContext.class,
              EmailAccessPointAdministrationResource.class,
              EmailAccessPointsAdministrationResource.class,
              ProxyUserContext.class,
              ProxyUsersContext.class,
              ProxyUsersResource.class,
              SelectedTemplatesResource.class,
              SelectedTemplatesContext.class,
              OrganizationAttachmentsResource.class,
              OrganizationAttachmentsContext.class,

              // Crystal
              CrystalContext.class,
              CrystalResource.class
      );

      module.values(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

   private void addResourceContexts(ModuleAssembly module, Class<?>... resourceContextClasses) throws AssemblyException
   {
      for (Class<?> resourceContextClass : resourceContextClasses)
      {
         if (CommandQueryResource.class.isAssignableFrom(resourceContextClass))
         {
            module.objects(resourceContextClass);
         } else if (TransientComposite.class.isAssignableFrom(resourceContextClass))
         {
            module.transients((Class<TransientComposite>) resourceContextClass);
         } else
         {
            module.objects(resourceContextClass);
         }
      }
   }
}
