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

package se.streamsource.streamflow.api.assembler;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.api.administration.ChangePasswordDTO;
import se.streamsource.streamflow.api.administration.LinkTree;
import se.streamsource.streamflow.api.administration.NewProxyUserDTO;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserListDTO;
import se.streamsource.streamflow.api.administration.RegisterUserDTO;
import se.streamsource.streamflow.api.administration.UserEntityDTO;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.FormValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignaturesValue;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
import se.streamsource.streamflow.api.administration.surface.EmailAccessPointDTO;
import se.streamsource.streamflow.api.administration.surface.SelectedTemplatesDTO;
import se.streamsource.streamflow.api.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.api.workspace.PerspectiveDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
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
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;

/**
 * Assembler for the Streamflow Client API.
 */
public class ClientAPIAssembler
        implements Assembler
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      new ValueAssembler().assemble( module );
      workspace(module);
      overview(module);
      administration(module);
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
              AttachmentDTO.class, UpdateAttachmentDTO.class);

      workspace.values(FieldDTO.class,
              FormDraftDTO.class,
              PageSubmissionDTO.class,
              FieldSubmissionDTO.class,
              SubmittedFormDTO.class,
              SubmittedFormListDTO.class,
              SubmittedFormsListDTO.class,
              SubmittedPageDTO.class,
              FieldValueDTO.class,
              AttachmentFieldSubmission.class,
              AttachmentFieldDTO.class,
              FormSignatureDTO.class
      );

      workspace.values(ContactAddressDTO.class,
              ContactEmailDTO.class,
              ContactPhoneDTO.class,
              ContactDTO.class);
   }

   private void overview(ModuleAssembly overview)
   {
      overview.values(ProjectSummaryDTO.class);
   }

   private void administration(ModuleAssembly administration)
   {
      // Commands
      administration.values(
              RegisterUserDTO.class,
              ChangePasswordDTO.class,
              NewUserDTO.class,
              NewProxyUserDTO.class,
              CreateFieldDTO.class);

      // Queries
      administration.values(LinkTree.class, UserEntityDTO.class, ProxyUserListDTO.class, ProxyUserDTO.class);

      // Forms
      administration.values(
              FormValue.class,
              PageDefinitionValue.class,
              FieldValue.class,
              FieldDefinitionValue.class,
              RequiredSignaturesValue.class,
              RequiredSignatureValue.class,

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
              TextFieldValue.class);

      // Surface
      administration.values(EmailAccessPointDTO.class,
              AccessPointDTO.class,
              SelectedTemplatesDTO.class
      );
   }
}
