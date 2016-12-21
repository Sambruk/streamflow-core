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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.surface.AccessPointSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
public class SurfaceFormDraftContext
      implements IndexContext<FormDraftDTO>
{
   @Structure
   Module module;

   @Service
   SystemDefaultsService systemDefaults;
   
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

   public SecondSigneeInfoValue secondsigneeinfo() {
      FormDraft formDraft = RoleMap.role( FormDraft.class );
      
      // If it doesn't exist we create a empty one
      if (formDraft.getFormDraftValue().secondsignee().get() == null) 
      {
         ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );
         formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
      }
      return formDraft.getFormDraftValue().secondsignee().get();
   }
   
   public void updatesinglesignature( StringValue singleSignature )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );

      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );

      secondSigneeBuilder.prototype().singlesignature().set( new Boolean( "".equals( singleSignature.string().get() ) ? "false" : singleSignature.string().get() ) );

      formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
   }

   public void updatename( StringValue name )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );

      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );

      secondSigneeBuilder.prototype().name().set( name.string().get() );

      formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
   }

   public void updatesocialsecuritynumber( StringValue socialSecurityNumber )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );

      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );

      secondSigneeBuilder.prototype().socialsecuritynumber().set( socialSecurityNumber.string().get() );

      formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
   }

   public void updatephonenumber( StringValue phoneNumber )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );

      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );

      secondSigneeBuilder.prototype().phonenumber().set( phoneNumber.string().get() );

      formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
   }

   public void updateemail( StringValue email )
   {
      FormDraft formDraft = RoleMap.role( FormDraft.class );

      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder = getSecondSigneeInfoValueBuilder( formDraft );

      secondSigneeBuilder.prototype().email().set( email.string().get() );

      formDraft.addSecondSigneeInfo( secondSigneeBuilder.newInstance() );
   }

   private ValueBuilder<SecondSigneeInfoValue> getSecondSigneeInfoValueBuilder( FormDraft formDraft )
   {
      ValueBuilder<SecondSigneeInfoValue> secondSigneeBuilder;
      SecondSigneeInfoValue secondSignee = formDraft.getFormDraftValue().secondsignee().get();

      if( secondSignee == null )
      {
         secondSigneeBuilder = module.valueBuilderFactory().newValueBuilder( SecondSigneeInfoValue.class );
      } else
      {
         secondSigneeBuilder = secondSignee.buildWith();
      }
      return secondSigneeBuilder;
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
   
   public AccessPointSettingsDTO settings() {
      ValueBuilder<AccessPointSettingsDTO> builder = module.valueBuilderFactory().newValueBuilder( AccessPointSettingsDTO.class );
      builder.prototype().location().set( systemDefaults.config().configuration().mapDefaultStartLocation().get() );
      builder.prototype().zoomLevel().set( systemDefaults.config().configuration().mapDefaultZoomLevel().get() );
      
      return builder.newInstance();
   }
}