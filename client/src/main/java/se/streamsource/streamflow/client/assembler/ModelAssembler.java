/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import se.streamsource.streamflow.client.ui.administration.FormOnRemoveModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitModel;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseArchivalSettingModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseDefaultDaysToCompleteModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.FormOnCloseModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.PriorityOnCaseModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypeModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.external.IntegrationPointModel;
import se.streamsource.streamflow.client.ui.administration.external.IntegrationPointsModel;
import se.streamsource.streamflow.client.ui.administration.filters.ActionsModel;
import se.streamsource.streamflow.client.ui.administration.filters.FilterModel;
import se.streamsource.streamflow.client.ui.administration.filters.FiltersModel;
import se.streamsource.streamflow.client.ui.administration.filters.LabelRuleModel;
import se.streamsource.streamflow.client.ui.administration.filters.RulesModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldCreationModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldValueEditModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormPagesModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignatureModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.PageEditModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.SelectionElementsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersModel;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.priorities.PrioritiesModel;
import se.streamsource.streamflow.client.ui.administration.priorities.PriorityModel;
import se.streamsource.streamflow.client.ui.administration.projects.MembersModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.client.ui.administration.projectsettings.CaseDueOnNotificationModel;
import se.streamsource.streamflow.client.ui.administration.projectsettings.RecipientsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointModel;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.FormLabelsModel;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersModel;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesModel;
import se.streamsource.streamflow.client.ui.administration.users.UserAdministrationDetailModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationListModel;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CasesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.caselog.CaseLogModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageDraftModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormDraftModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.note.CaseNoteModel;
import se.streamsource.streamflow.client.ui.workspace.search.PerspectivesModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectivePeriodModel;
import se.streamsource.streamflow.client.util.LinksListModel;

import static org.qi4j.api.common.Visibility.*;
import static se.streamsource.streamflow.client.util.UIAssemblers.*;

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
            CaseLogModel.class,
            CaseLabelsModel.class,
            CaseSubmittedFormsModel.class,
            CaseSubmittedFormModel.class,
            FormSubmissionWizardPageModel.class,
            PossibleFormsModel.class,
            FormDraftModel.class,
            MessagesModel.class,
            MessageModel.class,
            MessageDraftModel.class,
            ConversationModel.class,
            ConversationsModel.class,
            ConversationParticipantsModel.class,
            AttachmentsModel.class,
            CaseNoteModel.class
      );

      module.values(CaseTableValue.class).visibleIn(Visibility.application);
   }

   private void administration(ModuleAssembly module) throws AssemblyException
   {
      /**for (Class aClass : filter(matches(".*Model"), getClasses(AdministrationModel.class)))
      {
         module.objects( aClass ).visibleIn(Visibility.layer);
      }*/

      addModels(module,
            AdministrationModel.class,
            OrganizationModel.class,
            OrganizationalUnitModel.class,
            CaseAccessDefaultsModel.class,
            CaseDefaultDaysToCompleteModel.class,
            CaseDueOnNotificationModel.class,
            RecipientsModel.class,
            CaseArchivalSettingModel.class,
            FormOnCloseModel.class,
            FormOnRemoveModel.class,
            CaseTypesModel.class,
            CaseTypeModel.class,
            AdministratorsModel.class,
            FieldValueEditModel.class,
            FieldCreationModel.class,
            FormsModel.class,
            FormModel.class,
            //FormElementsModel.class,
            FormSignaturesModel.class,
            FormSignatureModel.class,
            GroupsModel.class,
            GroupModel.class,
            LabelsModel.class,
            MembersModel.class,
            OrganizationsModel.class,
            OrganizationUsersModel.class,
            PageEditModel.class,
            ParticipantsModel.class,
            ProjectsModel.class,
            ProjectsModel.class,
            ProjectModel.class,
            RolesModel.class,
            ResolutionsModel.class,
            SelectedCaseTypesModel.class,
            SelectionElementsModel.class,
            SelectedLabelsModel.class,
            SelectedResolutionsModel.class,
            SelectedTemplatesModel.class,
            SelectedFormsModel.class,
            UsersAdministrationListModel.class,
            UserAdministrationDetailModel.class,
            FiltersModel.class,
            FilterModel.class,
            RulesModel.class,
            FormPagesModel.class,
            FormModel.class,
            ActionsModel.class,
            LabelRuleModel.class,
            PrioritiesModel.class,
            PriorityModel.class,
            PriorityOnCaseModel.class,
            FormLabelsModel.class);


      addModels(module, LinksListModel.class,
            UsersAndGroupsModel.class);

      // Surface
      addModels(module,
            AccessPointsModel.class,
            AccessPointModel.class,
            EmailAccessPointModel.class,
            EmailAccessPointsModel.class,
            ProxyUsersModel.class);

      // External
      addModels( module,
            IntegrationPointsModel.class,
            IntegrationPointModel.class);
   }
}
