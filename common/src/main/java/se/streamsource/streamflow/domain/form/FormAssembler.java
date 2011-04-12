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

package se.streamsource.streamflow.domain.form;

import org.qi4j.api.common.*;
import org.qi4j.bootstrap.*;

/**
 * JAVADOC
 */
public class FormAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
   {
      moduleAssembly.values( CreateFieldDTO.class,
            EffectiveFieldValue.class,
            EffectiveFormFieldsValue.class,
            SubmittedFieldValue.class,
            FormValue.class,
            FormDraftValue.class,
            FieldValue.class,
            FieldValueDTO.class,
            AttachmentFieldDTO.class,
            AttachmentFieldSubmission.class,
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
            FieldDefinitionValue.class,
            FieldSubmissionValue.class,
            SubmittedFormValue.class,
            SubmittedPageValue.class,
            PageSubmissionValue.class,
            FormSignatureValue.class,
            RequiredSignaturesValue.class,
            RequiredSignatureValue.class,
            PageDefinitionValue.class).visibleIn( Visibility.application );
   }
}
