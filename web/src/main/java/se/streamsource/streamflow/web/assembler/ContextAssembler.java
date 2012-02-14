/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.EntityAssembly;
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
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.surface.api.assembler.SurfaceAPIAssembler;
import se.streamsource.streamflow.util.ClassScanner;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

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
      new ValueAssembler().assemble(module);
      new SurfaceAPIAssembler().assemble(module);

      module.importedServices(InteractionConstraintsService.class).
              importedBy(NewObjectImporter.class).
              visibleIn(Visibility.application);
      module.objects(InteractionConstraintsService.class);

      module.objects(RequiresPermission.RequiresPermissionConstraint.class,
              ServiceAvailable.ServiceAvailableConstraint.class).visibleIn(Visibility.application);

      // Named queries
      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor("solrquery", "");
      namedQueries.addQuery(queryDescriptor);

      module.importedServices(NamedEntityFinder.class).
              importedBy(ServiceSelectorImporter.class).
              setMetaInfo(ServiceQualifier.withId("solr")).
              setMetaInfo(namedQueries);

      // Register all contexts
      for (Class aClass : Iterables.filter(ClassScanner.matches(".*Context"), ClassScanner.getClasses(LinksBuilder.class)))
      {
         addResourceContexts(module, aClass);
      }

/*      addResourceContexts(module,
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

              ArchivalSettingsContext.class,

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
              ClosedCaseResource.class,
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
      */

      module.values(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

   private Specification<EntityAssembly> assignableFrom(final Class<?> dataClass)
   {
      return new Specification<EntityAssembly>()
      {
         public boolean satisfiedBy(EntityAssembly item)
         {
            return dataClass.isAssignableFrom(item.type());
         }
      };
   }

   private void addResourceContexts(ModuleAssembly module, Class<?>... resourceContextClasses) throws AssemblyException
   {
      for (Class<?> resourceContextClass : resourceContextClasses)
      {
         if (CommandQueryResource.class.isAssignableFrom(resourceContextClass))
         {
            module.objects(resourceContextClass).visibleIn(Visibility.application);
         } else if (resourceContextClass.isInterface())
         {
            module.transients((Class<TransientComposite>) resourceContextClass).visibleIn(Visibility.application);
         } else
         {
            module.objects(resourceContextClass).visibleIn(Visibility.application);
         }
      }
   }
}
