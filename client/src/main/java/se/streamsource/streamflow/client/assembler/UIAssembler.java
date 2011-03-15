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

package se.streamsource.streamflow.client.assembler;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;
import static se.streamsource.streamflow.client.util.UIAssemblers.addDialogs;
import static se.streamsource.streamflow.client.util.UIAssemblers.addMV;
import static se.streamsource.streamflow.client.util.UIAssemblers.addModels;
import static se.streamsource.streamflow.client.util.UIAssemblers.addTasks;
import static se.streamsource.streamflow.client.util.UIAssemblers.addViews;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.restlet.Restlet;

import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.ui.ApplicationInitializationService;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.DummyDataService;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountSelectionView;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.account.AccountView;
import se.streamsource.streamflow.client.ui.account.AccountsDialog;
import se.streamsource.streamflow.client.ui.account.AccountsModel;
import se.streamsource.streamflow.client.ui.account.ChangePasswordDialog;
import se.streamsource.streamflow.client.ui.account.CreateAccountDialog;
import se.streamsource.streamflow.client.ui.account.ProfileModel;
import se.streamsource.streamflow.client.ui.account.ProfileView;
import se.streamsource.streamflow.client.ui.account.TestConnectionTask;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationTreeView;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.ui.administration.caseaccessdefaults.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.caseaccessdefaults.CaseAccessDefaultsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.forms.FormModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormView;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
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
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorListBoxFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorNumberFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorOpenSelectionFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorOptionButtonsFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorTextAreaFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldEditorTextFieldValueView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldValueEditModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FieldValueObserver;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormElementsModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormElementsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignatureModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignatureView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.PageEditModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.PageEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.SelectionElementsModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.SelectionElementsView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsModel;
import se.streamsource.streamflow.client.ui.administration.groups.ParticipantsView;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsView;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersModel;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationsModel;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationsView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.projects.MembersModel;
import se.streamsource.streamflow.client.ui.administration.projects.MembersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesView;
import se.streamsource.streamflow.client.ui.administration.surface.*;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesModel;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesView;
import se.streamsource.streamflow.client.ui.administration.templates.TemplatesView;
import se.streamsource.streamflow.client.ui.administration.users.CreateUserDialog;
import se.streamsource.streamflow.client.ui.administration.users.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationView;
import se.streamsource.streamflow.client.ui.menu.AccountMenu;
import se.streamsource.streamflow.client.ui.menu.AdministrationMenuBar;
import se.streamsource.streamflow.client.ui.menu.EditMenu;
import se.streamsource.streamflow.client.ui.menu.FileMenu;
import se.streamsource.streamflow.client.ui.menu.HelpMenu;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.ui.menu.ViewMenu;
import se.streamsource.streamflow.client.ui.menu.WindowMenu;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryModel;
import se.streamsource.streamflow.client.ui.overview.OverviewSummaryView;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseActionsView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseInfoView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.SubCasesView;
import se.streamsource.streamflow.client.ui.workspace.cases.actions.CaseActionsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactLookupResultDialog;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationParticipantsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesConversationView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseEffectiveFieldsValueModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseEffectiveFieldsValueView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.SubmittedFormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.AttachmentFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.CheckboxesPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.ComboBoxPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.DatePanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.ListBoxPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.NumberPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.OpenSelectionPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.OptionButtonsPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.TextAreaFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.TextFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.history.HistoryView;
import se.streamsource.streamflow.client.ui.workspace.cases.history.MessagesHistoryView;
import se.streamsource.streamflow.client.ui.workspace.context.WorkspaceContextModel2;
import se.streamsource.streamflow.client.ui.workspace.context.WorkspaceContextView2;
import se.streamsource.streamflow.client.ui.workspace.search.HandleSearchesDialog;
import se.streamsource.streamflow.client.ui.workspace.search.SaveSearchDialog;
import se.streamsource.streamflow.client.ui.workspace.search.SavedSearchesModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesDetailView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesView;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.ExceptionHandlerService;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.LinksListModel;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.client.util.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinksDialog;

/**
 * JAVADOC
 */
public class UIAssembler
{
   public void assemble( LayerAssembly layer ) throws AssemblyException
   {
      search( layer.module( "Search" ) );
      administration( layer.module( "Administration" ) );
      workspace( layer.module( "Workspace" ) );
      cases( layer.module( "Case" ) );
      menu( layer.module( "Menu" ) );
      overview( layer.module( "Overview" ) );
      streamflow( layer.module( "Streamflow" ) );
      restlet( layer.module( "Restlet client" ) );

      // More specific administration modules
      labels( layer.module( "Labels" ) );
      userAdministration( layer.module( "Users" ) );
      organizationAdministration( layer.module( "Organizations" ) );
      groupAdministration( layer.module( "Groups" ) );
      projectAdministration( layer.module( "Projects" ) );
      caseTypeAdministration( layer.module( "Case types" ) );
      resolutions( layer.module( "Resolutions" ) );
      roleAdministration( layer.module( "Roles" ) );
      forms( layer.module( "Forms" ) );
      administratorAdministrator( layer.module( "Administrators" ) );
      surfaceAdministration( layer.module( "Surface" ) );
      caseAccesDefaults(layer.module( "Case access defaults" ));
   }

   private void caseAccesDefaults( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      addMV( moduleAssembly, CaseAccessDefaultsModel.class, CaseAccessDefaultsView.class );
   }

   private void surfaceAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module,
            AccessPointsModel.class,
            AccessPointsView.class );

      addMV( module,
            AccessPointModel.class,
            AccessPointView.class );

      addMV( module,
            EmailAccessPointsModel.class,
            EmailAccessPointsView.class );

      addViews(module, EmailAccessPointView.class);

      addMV( module,
            ProxyUsersModel.class,
            ProxyUsersView.class );
   }

   private void administratorAdministrator( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, AdministratorsModel.class,
            AdministratorsView.class );
   }

   private void forms( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, SelectedFormsModel.class, SelectedFormsView.class );
   }

   private void roleAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, RolesModel.class,
            RolesView.class );
   }

   private void resolutions( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, ResolutionsModel.class, ResolutionsView.class );
      addMV( module, SelectedResolutionsModel.class, SelectedResolutionsView.class );
   }

   private void caseTypeAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, SelectedCaseTypesModel.class, SelectedCaseTypesView.class );

      addMV( module, FormsModel.class, FormsView.class );

      addMV( module, FormModel.class, FormView.class );

      addMV( module, CaseTypesModel.class,
            CaseTypesView.class );

   }

   private void projectAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, ProjectsModel.class,
            ProjectsView.class );

      addViews( module, FormEditView.class );

      addMV( module, MembersModel.class,
            MembersView.class );

      addMV( module,
            FormElementsModel.class, FormElementsView.class );

      addMV( module, FormSignaturesModel.class, FormSignaturesView.class );

      addMV( module, FormSignatureModel.class, FormSignatureView.class );

      addMV( module,
            SelectionElementsModel.class, SelectionElementsView.class );

      addMV( module,
            PageEditModel.class, PageEditView.class );

      addMV( module,
            FieldValueEditModel.class, FieldEditView.class );

      addViews( module,
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
            FieldEditorTextFieldValueView.class );

      addDialogs( module, FieldCreationDialog.class );

      module.objects( FieldValueObserver.class );

   }

   private void groupAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, GroupsModel.class,
            GroupsView.class );

      addMV( module, ParticipantsModel.class,
            ParticipantsView.class );

   }

   private void organizationAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, OrganizationsModel.class,
            OrganizationsView.class );
      addMV( module, OrganizationUsersModel.class,
            OrganizationUsersView.class );
   }

   private void userAdministration( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, UsersAdministrationModel.class,
            UsersAdministrationView.class );
   }

   private void labels( ModuleAssembly module ) throws AssemblyException
   {
      addMV( module, LabelsModel.class, LabelsView.class );

      addMV( module, SelectedLabelsModel.class, SelectedLabelsView.class );
   }

   private void restlet( ModuleAssembly module ) throws AssemblyException
   {
      module.importedServices( Restlet.class ).visibleIn( application );
   }

   private void streamflow( ModuleAssembly module ) throws AssemblyException
   {
      module.objects(
            StreamflowApplication.class,
            AccountSelector.class
      );

      // SAF objects
      module.importedServices( StreamflowApplication.class, ApplicationContext.class, AccountSelector.class ).visibleIn( layer );


      module.services( DummyDataService.class ).instantiateOnStartup();
      module.services( ApplicationInitializationService.class ).instantiateOnStartup();

      addDialogs( module, NameDialog.class,
            SelectUsersAndGroupsDialog.class,
            CreateUserDialog.class,
            CreateProxyUserDialog.class,
            ConfirmationDialog.class,
            ResetPasswordDialog.class );

      addModels( module, LinksListModel.class,
            UsersAndGroupsModel.class );

      module.objects( DebugWindow.class );

      module.objects(
            DialogService.class,
            UncaughtExceptionHandler.class,
            JavaHelp.class
      ).visibleIn( layer );

      module.importedServices( UncaughtExceptionHandler.class,
            JavaHelp.class ).importedBy( NewObjectImporter.class ).visibleIn( application );
      module.services(
            ExceptionHandlerService.class ).instantiateOnStartup();
      module.importedServices( DialogService.class ).importedBy( NewObjectImporter.class ).visibleIn( application );

      module.objects( ActionBinder.class, ValueBinder.class, StateBinder.class ).visibleIn( layer );
   }

   private void overview( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( OverviewWindow.class ).visibleIn( layer );

      addMV( module,
            OverviewModel.class,
            OverviewView.class );

      addMV( module,
            OverviewSummaryModel.class,
            OverviewSummaryView.class );
   }

   private void cases( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, CasesView.class, CasesDetailView.class, ContactsAdminView.class,
            FormsAdminView.class, SubmittedFormsAdminView.class, CheckboxesPanel.class,
            ComboBoxPanel.class, OptionButtonsPanel.class, OpenSelectionPanel.class, ListBoxPanel.class, DatePanel.class,
            NumberPanel.class, TextAreaFieldPanel.class, TextFieldPanel.class, AttachmentFieldPanel.class, 
            HistoryView.class, MessagesHistoryView.class
      );

      addDialogs( module, ContactLookupResultDialog.class );

      addMV( module, CasesTableModel.class, CasesTableView.class );

      addMV( module, CaseModel.class, CaseInfoView.class );

      addViews( module,
            CaseDetailView.class );

      addViews( module, SubCasesView.class );

      addMV( module,
            ContactsModel.class,
            ContactsView.class );

      addMV( module,
            ContactModel.class,
            ContactView.class );

      addMV( module,
            CaseGeneralModel.class,
            CaseGeneralView.class );

      addMV( module,
            CaseLabelsModel.class,
            CaseLabelsView.class );

      addMV( module,
            CaseEffectiveFieldsValueModel.class,
            CaseEffectiveFieldsValueView.class );

      addMV( module,
            CaseSubmittedFormsModel.class,
            CaseSubmittedFormsView.class );

      addMV( module,
            CaseSubmittedFormModel.class,
            CaseSubmittedFormView.class );

      addMV( module,
            FormSubmissionWizardPageModel.class,
            FormSubmissionWizardPageView.class );

      addMV( module,
            PossibleFormsModel.class,
            PossibleFormsView.class );

      addMV( module,
            CaseActionsModel.class,
            CaseActionsView.class );

      // conversations
      addMV( module,
            MessagesModel.class,
            MessagesConversationView.class );

      addViews( module,
            ConversationView.class );

      addMV( module,
            ConversationsModel.class,
            ConversationsView.class );

      addMV( module,
            ConversationParticipantsModel.class,
            ConversationParticipantsView.class );

      // Attachments
      addMV( module,
            AttachmentsModel.class,
            AttachmentsView.class );
   }

   private void workspace( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module, AccountSelectionView.class );
      module.objects( WorkspaceWindow.class ).visibleIn( layer );

      addViews( module,
            WorkspaceView.class );

      addMV( module,
            WorkspaceContextModel2.class,
            WorkspaceContextView2.class );

      addMV( module, SavedSearchesModel.class, SearchView.class );

      addDialogs( module, SelectLinkDialog.class,
            SaveSearchDialog.class,
            HandleSearchesDialog.class );

      module.values( CaseTableValue.class ).visibleIn( Visibility.application );
   }

   private void menu( ModuleAssembly module ) throws AssemblyException
   {
      addViews( module,
            WorkspaceMenuBar.class,
            OverviewMenuBar.class,
            AdministrationMenuBar.class );
      addViews( module,
            FileMenu.class,
            EditMenu.class,
            ViewMenu.class,
            AccountMenu.class,
            WindowMenu.class,
            HelpMenu.class
      );

      addDialogs( module, CreateAccountDialog.class, AccountsDialog.class );

      addModels( module, AccountsModel.class );
   }

   private void administration( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( AdministrationWindow.class ).visibleIn( layer );

      addViews( module,
            AdministrationView.class, TabbedResourceView.class );

      addMV( module,
            AdministrationModel.class,
            AdministrationTreeView.class );

      addMV( module, ProfileModel.class, ProfileView.class );

      addMV( module,
            AccountModel.class,
            AccountView.class );

      addDialogs( module,
            ChangePasswordDialog.class,
            SelectLinksDialog.class );
      addTasks( module, TestConnectionTask.class );

      addViews( module, TemplatesView.class );

      addMV( module,
            SelectedTemplatesModel.class,
            SelectedTemplatesView.class );

   }

   private void search( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( SearchResultTableModel.class ).visibleIn( layer );
   }
}
