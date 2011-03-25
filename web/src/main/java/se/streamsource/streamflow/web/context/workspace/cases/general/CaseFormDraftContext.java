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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.domain.form.AttachmentFieldDTO;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;

import java.io.IOException;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class CaseFormDraftContext
   implements DeleteContext, IndexContext<FormDraftValue>
{
   public FormDraftValue index()
   {
      FormDraft formDraft = role( FormDraft.class);

      return formDraft.getFormDraftValue();
   }

   public void updatefield( FieldValueDTO field )
   {
      FormDraft formDraft = role( FormDraft.class);
      formDraft.changeFieldValue( field.field().get(), field.value().get() );
   }

   public void updateattachmentfield( AttachmentFieldDTO fieldAttachment )
   {
      FormDraft formDraft = role( FormDraft.class);
      formDraft.changeFieldAttachmentValue( fieldAttachment );
   }

   public void submit()
   {
      FormDraft formDraft = role( FormDraft.class);

      Submitter submitter = role( Submitter.class );

      role( SubmittedForms.class ).submitForm( formDraft, submitter );
   }

   public void delete() throws ResourceException, IOException
   {
      FormDrafts formDrafts = role( FormDrafts.class );
      FormDraft formDraft = role( FormDraft.class);
      formDrafts.discardFormDraft( formDraft );
   }
}
