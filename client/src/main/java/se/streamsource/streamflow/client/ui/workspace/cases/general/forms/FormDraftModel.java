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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;

/**
 * Model for a form draft. Use this to update a draft until it is to be submitted
 */
public class FormDraftModel
{
   @Uses
   CommandQueryClient client;

   public FormDraftDTO getFormDraftDTO()
   {
      return client.query("index", FormDraftDTO.class);
   }

   public void updateField(FieldValueDTO fieldValueDTO)
   {
      client.putCommand("updatefield", fieldValueDTO);
   }

   public void updateAttachment(String attachmentId, UpdateAttachmentDTO updateAttachmentDTO)
   {
      client.getClient( "formattachments/" + attachmentId +"/" ).postCommand( "update", updateAttachmentDTO );
   }

   public void updateAttachmentField(AttachmentFieldDTO attachmentFieldDTO)
   {
      client.putCommand( "updateattachmentfield",  attachmentFieldDTO );
   }

   public void createAttachment(Representation input)
   {
      client.getClient( "formattachments/" ).postCommand( "createformattachment", input);
   }

   public void submit()
   {
      client.putCommand( "submit" );
   }

   public void delete()
   {
      client.delete();
   }
}
