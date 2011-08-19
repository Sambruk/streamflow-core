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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
public class SurfaceFormDraftContext
      implements IndexContext<FormDraftDTO>
{
   @Structure
   Module module;

   public FormDraftDTO index()
   {
      return RoleMap.role( FormDraftDTO.class );
   }

   public void updatefield( FieldValueDTO field )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );
      formDraft.changeFieldValue( field.field().get(), field.value().get() );
   }

   public void updateattachmentfield( AttachmentFieldDTO fieldAttachment )
   {
      FormDraft formDraft = role( FormDraft.class);
      formDraft.changeFieldAttachmentValue( fieldAttachment );
   }

   public FieldValueDTO fieldvalue( StringValue fieldId )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );
      EntityReference entityReference = EntityReference.parseEntityReference(fieldId.string().get());
      FieldSubmissionDTO DTO = formDraft.getFieldValue( entityReference );

      ValueBuilder<FieldValueDTO> builder = module.valueBuilderFactory().newValueBuilder(FieldValueDTO.class);
      builder.prototype().value().set( DTO.value().get() == null ? "" : DTO.value().get() );
      builder.prototype().field().set( entityReference );
      
      return builder.newInstance();
   }


   public void addsignature( FormSignatureDTO signature )
   {
      FormDraft formDraft = role( FormDraft.class );
      formDraft.addFormSignatureValue( signature );
   }

   public void removeSignatures()
   {
      FormDraft formDraft = role( FormDraft.class );
      formDraft.removeFormSignatures();
   }

   /**
    * discard form and remove case
    */
   public void discard()
   {
      FormDraft formSubmission = RoleMap.role( FormDraft.class );
      FormDrafts data = RoleMap.role( FormDrafts.class );
      data.discardFormDraft( formSubmission );

      EndUserCases cases = RoleMap.role( EndUserCases.class );
      cases.discardCase( RoleMap.role( Case.class ) );
   }
}