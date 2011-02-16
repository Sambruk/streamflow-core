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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.domain.form.AttachmentFieldDTO;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.domain.form.FormSignatureValue;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

import se.streamsource.dci.value.StringValue;
import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
public class SurfaceFormDraftContext
      implements IndexContext<FormDraftValue>
{
   @Structure
   ValueBuilderFactory vbf;

   public FormDraftValue index()
   {
      return RoleMap.role( FormDraftValue.class );
   }

   public void updatefield( FieldDTO field )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );
      formDraft.changeFieldValue( EntityReference.parseEntityReference( field.field().get() ), field.value().get() );
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
      FieldSubmissionValue value = formDraft.getFieldValue( entityReference );

      ValueBuilder<FieldValueDTO> builder = vbf.newValueBuilder( FieldValueDTO.class );
      builder.prototype().value().set( value.value().get() == null ? "" : value.value().get() );
      builder.prototype().field().set( entityReference );
      
      return builder.newInstance();
   }


   public void addsignature( FormSignatureValue signature )
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