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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.ui.ApplicationInitializationService;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.account.AccountSelectionView;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.account.AccountView;
import se.streamsource.streamflow.client.ui.account.AccountsDialog;
import se.streamsource.streamflow.client.ui.account.ChangePasswordDialog;
import se.streamsource.streamflow.client.ui.account.CreateAccountDialog;
import se.streamsource.streamflow.client.ui.account.ProfileView;
import se.streamsource.streamflow.client.ui.account.TestConnectionTask;
import se.streamsource.streamflow.client.ui.administration.AdministrationTreeView;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.administration.FormOnRemoveView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseArchivalSettingView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseDefaultDaysToCompleteView;
import se.streamsource.streamflow.client.ui.administration.casesettings.FormOnCloseView;
import se.streamsource.streamflow.client.ui.administration.casesettings.PriorityOnCaseView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.filters.ActionsView;
import se.streamsource.streamflow.client.ui.administration.filters.FilterView;
import se.streamsource.streamflow.client.ui.administration.filters.FiltersView;
import se.streamsource.streamflow.client.ui.administration.filters.LabelRuleView;
import se.streamsource.streamflow.client.ui.administration.filters.RulesView;
import se.streamsource.streamflow.client.ui.administration.forms.FormView;
import se.streamsource.streamflow.client.ui.administration.forms.FormsView;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldCreationDialog;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorAttachmentFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorCheckboxesFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorComboBoxFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorCommentFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorDateFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorFieldGroupValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorListBoxFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorNumberFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorOpenSelectionFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorOptionButtonsFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorTextAreaFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorTextFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldValueObserver;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormElementsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignatureView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.PageEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.SelectionElementsView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsView;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsView;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.priorities.PrioritiesView;
import se.streamsource.streamflow.client.ui.administration.priorities.PriorityView;
import se.streamsource.streamflow.client.ui.administration.projects.MembersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.projectsettings.CaseDueOnNotificationView;
import se.streamsource.streamflow.client.ui.administration.projectsettings.RecipientsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;
import se.streamsource.streamflow.client.ui.administration.roles.RolesView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.CreateProxyUserDialog;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointView;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersView;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesView;
import se.streamsource.streamflow.client.ui.administration.templates.TemplatesView;
import se.streamsource.streamflow.client.ui.administration.users.CreateUserDialog;
import se.streamsource.streamflow.client.ui.administration.users.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.administration.users.UserAdministrationDetailView;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationListView;
import se.streamsource.streamflow.client.ui.menu.AccountMenu;
import se.streamsource.streamflow.client.ui.menu.AdministrationMenuBar;
import se.streamsource.streamflow.client.ui.menu.EditMenu;
import se.streamsource.streamflow.client.ui.menu.FileMenu;
import se.streamsource.streamflow.client.ui.menu.HelpMenu;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.ui.menu.PerspectiveMenu;
import se.streamsource.streamflow.client.ui.menu.ViewMenu;
import se.streamsource.streamflow.client.ui.menu.WindowMenu;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryView;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceContextView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseActionsView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseInfoView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.PdfPrintingDialog;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.caselog.CaseLogView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactLookupResultDialog;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationParticipantsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.CreateExternalMailUserDialog;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageAttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageDraftAttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageDraftView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessageView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesConversationView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.SubmittedFormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.AttachmentFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.CheckboxesPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.ComboBoxPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.DatePanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.ListBoxPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.NumberPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.OpenSelectionPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.OptionButtonsPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.TextAreaFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.TextFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.note.CaseNoteView;
import se.streamsource.streamflow.client.ui.workspace.search.ManagePerspectivesDialog;
import se.streamsource.streamflow.client.ui.workspace.search.SearchView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesDetailView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesView;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectivePeriodView;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectiveView;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.ExceptionHandlerService;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.client.util.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.InputDialog;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinksDialog;

import static org.qi4j.api.common.Visibility.*;
import static se.streamsource.streamflow.client.util.UIAssemblers.*;

/**
 * JAVADOC
 */
public class UIAssembler
{
   public void assemble(LayerAssembly layer) throws AssemblyException
   {
      menu(layer.module("Menu"));
      client(layer.module("Client"));
      account(layer.module("Account"));
      workspace(layer.module("Workspace"));
      overview(layer.module("Overview"));
      administration(layer.module("Administration"));
   }

   private void account(ModuleAssembly module) throws AssemblyException
   {
      addViews(module, AccountView.class, ProfileView.class);
   }

   private void client(ModuleAssembly module) throws AssemblyException
   {
      module.objects(
            StreamflowApplication.class,
            AccountSelector.class
      );

      // SAF objects
      module.importedServices(StreamflowApplication.class, ApplicationContext.class, AccountSelector.class).visibleIn(layer);

      module.services(ApplicationInitializationService.class).instantiateOnStartup();

      addDialogs(module, InputDialog.class,
            NameDialog.class,
            SelectUsersAndGroupsDialog.class,
            CreateUserDialog.class,
            CreateProxyUserDialog.class,
            ConfirmationDialog.class,
            ResetPasswordDialog.class);

      module.objects(DebugWindow.class);

      module.objects(
            DialogService.class,
            UncaughtExceptionHandler.class,
            JavaHelp.class
      ).visibleIn(layer);

      module.importedServices(UncaughtExceptionHandler.class,
            JavaHelp.class).importedBy(NewObjectImporter.class).visibleIn(application);
      module.services(
            ExceptionHandlerService.class).instantiateOnStartup();
      module.importedServices(DialogService.class).importedBy(NewObjectImporter.class).visibleIn(application);

      module.objects(ActionBinder.class, ValueBinder.class, StateBinder.class).visibleIn(layer);

      addViews(module, AccountSelectionView.class);
   }

   private void overview(ModuleAssembly module) throws AssemblyException
   {
      module.objects(OverviewWindow.class).visibleIn(layer);

      addViews(module,
            OverviewView.class,
            OverviewSummaryView.class);
   }

   private void workspace(ModuleAssembly module) throws AssemblyException
   {
      //views(module, filter(matches(".*View|.*Panel"), getClasses(WorkspaceView.class)));


      addViews( module,
            WorkspaceView.class,
            WorkspaceContextView.class,
            SearchView.class,
            CasesView.class,
            CasesTableView.class,
            PerspectivePeriodView.class );

      addViews( module,
            CaseActionsView.class,
            CaseInfoView.class,
            CasesDetailView.class,
            CaseDetailView.class,
            ContactsAdminView.class,
            ContactsView.class,
            ContactView.class,
            CaseGeneralView.class,
            CaseLogView.class,
            CaseLabelsView.class,
            CaseSubmittedFormsView.class,
            CaseSubmittedFormView.class,
            SubmittedFormsAdminView.class,
            FormSubmissionWizardPageView.class,
            PossibleFormsView.class,
            PossibleFormView.class,
            CheckboxesPanel.class,
            ComboBoxPanel.class,
            OptionButtonsPanel.class,
            OpenSelectionPanel.class,
            ListBoxPanel.class,
            DatePanel.class,
            NumberPanel.class,
            TextAreaFieldPanel.class,
            TextFieldPanel.class,
            AttachmentFieldPanel.class,
            MessagesConversationView.class,
            ConversationsView.class,
            ConversationView.class,
            ConversationParticipantsView.class,
            MessagesView.class,
            MessageView.class,
            MessageAttachmentsView.class,
            MessageDraftView.class,
            MessageDraftAttachmentsView.class,
            AttachmentsView.class,
            PerspectiveView.class,
            CaseNoteView.class
      );


      addDialogs(module, ContactLookupResultDialog.class);

      module.objects(WorkspaceWindow.class).visibleIn(layer);

      addDialogs(module, SelectLinkDialog.class,
            CreateExternalMailUserDialog.class,
            ManagePerspectivesDialog.class,
            PdfPrintingDialog.class);

      module.values(CaseTableValue.class).visibleIn(Visibility.application);
   }

   private void menu(ModuleAssembly module) throws AssemblyException
   {
      addViews(module,
            WorkspaceMenuBar.class,
            OverviewMenuBar.class,
            AdministrationMenuBar.class);
      addViews(module,
            FileMenu.class,
            EditMenu.class,
            ViewMenu.class,
            AccountMenu.class,
            WindowMenu.class,
            HelpMenu.class,
            PerspectiveMenu.class
      );

      addDialogs(module, CreateAccountDialog.class, AccountsDialog.class);
   }

   private void administration(ModuleAssembly module) throws AssemblyException
   {
      //views(module, Iterables.filter(matches(".*View"), getClasses(AdministrationView.class)));


      module.objects(ProjectModel.class ).visibleIn(layer);
      
      addMV(module, SelectedFormsModel.class, SelectedFormsView.class);

      addViews(module,
            AdministratorsView.class,
            AdministrationView.class,
            AdministrationTreeView.class,
            ResolutionsView.class,
            RolesView.class,
            SelectedCaseTypesView.class,
            FormsView.class,
            FormView.class,
            GroupsView.class,
            ParticipantsView.class,
            ProjectsView.class,
            LabelsView.class,
            MembersView.class,
            FormElementsView.class,
            FormSignatureView.class,
            FormSignaturesView.class,
            SelectionElementsView.class,
            PageEditView.class,
            OrganizationUsersView.class,
            FieldEditView.class,
            CaseTypesView.class,
            SelectedFormsView.class,
            SelectedLabelsView.class,
            SelectedResolutionsView.class,
            CaseAccessDefaultsView.class,
            CaseDefaultDaysToCompleteView.class,
            CaseDueOnNotificationView.class,
            RecipientsView.class,
            CaseArchivalSettingView.class,
            FormOnCloseView.class,
            FormOnRemoveView.class,
            UserAdministrationDetailView.class,
            UsersAdministrationListView.class,
            ProxyUsersView.class,
            AccessPointsView.class,
            AccessPointView.class,
            TemplatesView.class,
            SelectedTemplatesView.class,
            TabbedResourceView.class,
            EmailAccessPointsView.class,
            EmailAccessPointView.class,
            FiltersView.class,
            FilterView.class,
            RulesView.class,
            FormEditView.class,
            ActionsView.class,
            LabelRuleView.class,
            PrioritiesView.class,
            PriorityView.class,
            PriorityOnCaseView.class);

      addViews(module,
            FieldEditorAttachmentFieldValueView.class,
            FieldEditorCheckboxesFieldValueView.class,
            FieldEditorComboBoxFieldValueView.class,
            FieldEditorCommentFieldValueView.class,
            FieldEditorDateFieldValueView.class,
            FieldEditorListBoxFieldValueView.class,
            FieldEditorNumberFieldValueView.class,
            FieldEditorOptionButtonsFieldValueView.class,
            FieldEditorOpenSelectionFieldValueView.class,
            FieldEditorTextAreaFieldValueView.class,
            FieldEditorTextFieldValueView.class,
            FieldEditorFieldGroupValueView.class);

      addDialogs(module, FieldCreationDialog.class);

      module.objects(FieldValueObserver.class);

      addViews(module, TabbedResourceView.class);

      module.objects(AdministrationWindow.class).visibleIn(layer);

      addDialogs(module,
            ChangePasswordDialog.class,
            SelectLinksDialog.class);
      addTasks(module, TestConnectionTask.class);
   }
}
