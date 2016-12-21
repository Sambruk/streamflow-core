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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftSettingsDTO;

/**
 * Model for a form draft. Use this to update a draft until it is to be submitted
 */
public class FormDraftModel
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

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

   public String kartagoclientexe(LinkValue link)
   {
      StringValue value = client.queryLink( link, StringValue.class );
      return value.string().get();
   }

   public boolean isStreetLookupEnabled()
   {
      ResourceValue resource = client.query();
      return Iterables.matchesAny( Links.withRel("searchstreets"), resource.queries().get() );
   }

   public StreetsDTO searchStreets( String query ) throws ResourceException
   {
      ValueBuilder<StreetSearchDTO> builder = module.valueBuilderFactory().newValueBuilder( StreetSearchDTO.class );
      builder.prototype().address().set( query );
      return client.query( "searchstreets", StreetsDTO.class, builder.newInstance());
   }

   public void submit()
   {
      client.putCommand( "submit" );
   }

   public void delete()
   {
      client.delete();
   }

   public FormDraftSettingsDTO settings()
   {
      return client.query("settings", FormDraftSettingsDTO.class);
   }
}
