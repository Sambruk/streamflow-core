/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.api.assembler;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import se.streamsource.streamflow.api.administration.*;
import se.streamsource.streamflow.api.administration.filter.AssignActionValue;
import se.streamsource.streamflow.api.administration.filter.ChangeOwnerActionValue;
import se.streamsource.streamflow.api.administration.filter.CloseActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailNotificationActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.api.administration.form.CreateFieldGroupDTO;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.FormValue;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignaturesValue;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.api.administration.priority.PriorityDTO;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
import se.streamsource.streamflow.api.administration.surface.EmailAccessPointDTO;
import se.streamsource.streamflow.api.administration.surface.SelectedTemplatesDTO;
import se.streamsource.streamflow.api.external.ContentValue;
import se.streamsource.streamflow.api.external.IntegrationPointDTO;
import se.streamsource.streamflow.api.external.LogValue;
import se.streamsource.streamflow.api.external.ShadowCaseDTO;
import se.streamsource.streamflow.api.external.ShadowCaseLinkValue;
import se.streamsource.streamflow.api.interaction.profile.UserProfileDTO;
import se.streamsource.streamflow.api.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.api.surface.AccessPointSettingsDTO;
import se.streamsource.streamflow.api.workspace.PerspectiveDTO;
import se.streamsource.streamflow.api.workspace.ProjectListValue;
import se.streamsource.streamflow.api.workspace.SearchResultDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogFilterValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.ExternalEmailValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormsListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.api.workspace.cases.general.CaseGeneralDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionPluginDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValuesDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.NoteDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PermissionsDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;

/**
 * Assembler for the Streamflow Client API.
 */
public class ClientAPIAssembler
        implements Assembler
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      workspace(module);
      overview(module);
      administration(module);
      external(module);
      surface(module);
   }

   private void surface( ModuleAssembly surface)
   {
      surface.values( AccessPointSettingsDTO.class );
   }

   private void external( ModuleAssembly external )
   {
      external.values( ShadowCaseLinkValue.class,
            ShadowCaseDTO.class,
            ContentValue.class,
            LogValue.class,
            IntegrationPointDTO.class );
   }

   private void workspace(ModuleAssembly workspace)
   {
      workspace.values(PerspectiveDTO.class);

      workspace.values(CaseDTO.class,
              CaseOutputConfigDTO.class,
              CaseGeneralDTO.class,
              ContactsDTO.class,
              ConversationDTO.class,
              MessageDTO.class,
              AttachmentDTO.class,
              UpdateAttachmentDTO.class,
              CaseLogEntryDTO.class,
              CaseLogFilterValue.class,
              NoteDTO.class,
              ExternalEmailValue.class,
              PermissionsDTO.class,
              PriorityValue.class,
              PriorityDTO.class,
              ProjectListValue.class,
              SearchResultDTO.class);

      workspace.values(FieldDTO.class,
              FormDraftDTO.class,
              FormDraftSettingsDTO.class,
              PageSubmissionDTO.class,
              FieldSubmissionDTO.class,
              FieldSubmissionPluginDTO.class,
              SubmittedFormDTO.class,
              SubmittedFormListDTO.class,
              SubmittedFormsListDTO.class,
              SubmittedPageDTO.class,
              FieldValueDTO.class,
              FieldValuesDTO.class,
              AttachmentFieldSubmission.class,
              AttachmentFieldDTO.class,
              FormSignatureDTO.class,
              SecondSigneeInfoValue.class
      );

      workspace.values(ContactAddressDTO.class,
              ContactEmailDTO.class,
              ContactPhoneDTO.class,
              ContactDTO.class,
              StreetsDTO.class,
              StreetSearchDTO.class,
              UserProfileDTO.class);

      workspace.values(LocationDTO.class);
   }

   private void overview(ModuleAssembly overview)
   {
      overview.values(ProjectSummaryDTO.class);
   }

   private void administration(ModuleAssembly administration)
   {
      // Commands
      administration.values(
              ArchivalSettingsDTO.class,
              DueOnNotificationSettingsDTO.class,
              RequiresCaseTypeDTO.class,
              RegisterUserDTO.class,
              ChangePasswordDTO.class,
              NewUserDTO.class,
              NewProxyUserDTO.class,
              CreateFieldDTO.class,
              CreateFieldGroupDTO.class);

      // Queries
      administration.values(LinkTree.class, UserEntityDTO.class, ProxyUserListDTO.class, ProxyUserDTO.class, CaseTypeEntityDTO.class);

      // Filters
      administration.values(FilterValue.class);
      administration.values(LabelRuleValue.class); // Rules
      administration.values(AssignActionValue.class, ChangeOwnerActionValue.class, EmailActionValue.class, EmailNotificationActionValue.class, CloseActionValue.class); // Actions

      // Forms
      administration.values(
              FormValue.class,
              PageDefinitionValue.class,
              FieldValue.class,
              FieldDefinitionValue.class,
              FieldDefinitionAdminValue.class,
              RequiredSignaturesValue.class,
              RequiredSignatureValue.class,
              VisibilityRuleDefinitionValue.class,

              // Field types
              AttachmentFieldValue.class,
              CheckboxesFieldValue.class,
              ComboBoxFieldValue.class,
              CommentFieldValue.class,
              DateFieldValue.class,
              ListBoxFieldValue.class,
              NumberFieldValue.class,
              OptionButtonsFieldValue.class,
              OpenSelectionFieldValue.class,
              SelectionFieldValue.class,
              TextAreaFieldValue.class,
              TextFieldValue.class,
              FieldGroupFieldValue.class,
              GeoLocationFieldValue.class);

      // Surface
      administration.values(EmailAccessPointDTO.class,
              AccessPointDTO.class,
              SelectedTemplatesDTO.class
      );
   }
}
