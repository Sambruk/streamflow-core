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

package se.streamsource.streamflow.client.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.specification.Specifications;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountsModel;
import se.streamsource.streamflow.client.ui.account.ProfileModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CasesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormDraftModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsModel;
import se.streamsource.streamflow.client.ui.workspace.search.PerspectivesModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectivePeriodModel;
import se.streamsource.streamflow.client.util.LinksListModel;

import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.api.util.Iterables.filter;
import static se.streamsource.streamflow.client.util.UIAssemblers.addModels;
import static se.streamsource.streamflow.util.ClassScanner.getClasses;
import static se.streamsource.streamflow.util.ClassScanner.matches;

/**
 * TODO
 */
public class ModelAssembler
{
   public void assemble(LayerAssembly layer) throws AssemblyException
   {
      account(layer.module("Account"));
      administration(layer.module("Administration"));
      search(layer.module("Search"));
      workspace(layer.module("Workspace"));
      overview(layer.module("Overview"));

      // Everything has application visibility
      layer.objects(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

   private void search(ModuleAssembly module)
   {
      module.objects(SearchResultTableModel.class).visibleIn(layer);
   }

   private void account(ModuleAssembly module) throws AssemblyException
   {
      addModels(module, AccountsModel.class, AccountModel.class, ProfileModel.class);
   }

   private void overview(ModuleAssembly module) throws AssemblyException
   {
      addModels(module, OverviewModel.class, OverviewSummaryModel.class);
   }

   private void workspace(ModuleAssembly module) throws AssemblyException
   {
/*
      for (Class aClass : filter(matches(".*Model"), getClasses(WorkspaceModel.class)))
      {
         module.objects( aClass ).visibleIn(Visibility.layer);
      }
*/

      addModels(module,
            WorkspaceModel.class,
            PerspectivesModel.class,
            CasesModel.class,
            CasesTableModel.class,
            PerspectivePeriodModel.class);

      addModels(module,
            CaseModel.class,
            ContactsModel.class,
            ContactModel.class,
            CaseGeneralModel.class,
            CaseLabelsModel.class,
            CaseSubmittedFormsModel.class,
            CaseSubmittedFormModel.class,
            FormSubmissionWizardPageModel.class,
            PossibleFormsModel.class,
            FormDraftModel.class,
            MessagesModel.class,
            ConversationModel.class,
            ConversationsModel.class,
            ConversationParticipantsModel.class,
            AttachmentsModel.class
      );

      module.values(CaseTableValue.class).visibleIn(Visibility.application);
   }

   private void administration(ModuleAssembly module) throws AssemblyException
   {
      for (Class aClass : filter(matches(".*Model"), getClasses(AdministrationModel.class)))
      {
         module.objects( aClass ).visibleIn(Visibility.layer);
      }

 /*     addModels(module,
            AdministrationModel.class,
            OrganizationModel.class,
            OrganizationalUnitModel.class,
            CaseAccessDefaultsModel.class,
            CaseTypesModel.class,
            CaseTypeModel.class,
            AdministratorsModel.class,
            FieldValueEditModel.class,
            FormsModel.class,
            FormModel.class,
            FormElementsModel.class,
            FormSignaturesModel.class,
            FormSignatureModel.class,
            GroupsModel.class,
            LabelsModel.class,
            MembersModel.class,
            OrganizationsModel.class,
            OrganizationUsersModel.class,
            PageEditModel.class,
            ParticipantsModel.class,
            ProjectsModel.class,
            RolesModel.class,
            ResolutionsModel.class,
            SelectedCaseTypesModel.class,
            SelectionElementsModel.class,
            SelectedLabelsModel.class,
            SelectedResolutionsModel.class,
            SelectedTemplatesModel.class,
            SelectedFormsModel.class,
            UsersAdministrationModel.class);
*/

      addModels(module, LinksListModel.class,
            UsersAndGroupsModel.class);

      // Surface
/*      addModels(module,
            AccessPointsModel.class,
            AccessPointModel.class,
            EmailAccessPointModel.class,
            EmailAccessPointsModel.class,
            ProxyUsersModel.class);
            */
   }
}
