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

import static se.streamsource.dci.api.RoleMap.role;

import java.io.IOException;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.KnownDatatypeDefinitionUrls;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionPluginDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.infrastructure.plugin.KartagoPluginConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;

/**
 * JAVADOC
 */
public class CaseFormDraftContext implements DeleteContext, IndexContext<FormDraftDTO>
{

   @Service
   KartagoMapService kartagoMapService;

   @Structure
   Module module;

   public FormDraftDTO index()
   {
      FormDraft formDraft = role( FormDraft.class );

      FormDraftDTO draft = formDraft.getFormDraftValue();
      ValueBuilder<FormDraftDTO> draftBuilder = module.valueBuilderFactory().newValueBuilder( FormDraftDTO.class ).withPrototype( draft );

      int pageIndex = -1;
      for (PageSubmissionDTO pageSubmissionDTO : draft.pages().get())
      {
         pageIndex++;
         
         int fieldIndex = -1;
         for (FieldSubmissionDTO field : pageSubmissionDTO.fields().get())
         {
            fieldIndex++;
            if (KnownDatatypeDefinitionUrls.GEO_LOCATION.equals( field.field().get().datatypeUrl().get())
                  && ((KartagoPluginConfiguration) kartagoMapService.configuration()).enabled().get())
            {
               ValueBuilder<LinkValue> linkValueBuilder = module.valueBuilderFactory()
                     .newValueBuilder( LinkValue.class );
               linkValueBuilder.prototype().href().set( "kartagoclientexe" );
               linkValueBuilder.prototype().text().set( "Kartago Plugin" );
               linkValueBuilder.prototype().id().set( "kartagoplugin" );
               linkValueBuilder.prototype().rel().set( "kartagoplugin" );

               ValueBuilder<FieldSubmissionPluginDTO> fieldPluginBuilder = module.valueBuilderFactory()
                     .newValueBuilder( FieldSubmissionPluginDTO.class );
               fieldPluginBuilder.prototype().field().set( field.field().get() );
               fieldPluginBuilder.prototype().value().set( field.value().get());
               fieldPluginBuilder.prototype().message().set( field.message().get() );
               fieldPluginBuilder.prototype().enabled().set( field.enabled().get() );
               fieldPluginBuilder.prototype().plugin().set( linkValueBuilder.newInstance() );
               
               draftBuilder.prototype().pages().get().get( pageIndex ).fields().get().set( fieldIndex, fieldPluginBuilder.newInstance() );
            }
         }
      }
      return draftBuilder.newInstance();
   }

   public void updatefield(FieldValueDTO field)
   {
      FormDraft formDraft = role( FormDraft.class );
      formDraft.changeFieldValue( field.field().get(), field.value().get() );
   }

   public void updateattachmentfield(AttachmentFieldDTO fieldAttachment)
   {
      FormDraft formDraft = role( FormDraft.class );
      formDraft.changeFieldAttachmentValue( fieldAttachment );
   }

   public void submit()
   {
      FormDraft formDraft = role( FormDraft.class );

      Submitter submitter = role( Submitter.class );

      role( SubmittedForms.class ).submitForm( formDraft, submitter );
   }

   public void delete() throws IOException
   {
      FormDrafts formDrafts = role( FormDrafts.class );
      FormDraft formDraft = role( FormDraft.class );
      formDrafts.discardFormDraft( formDraft );
   }

   @ServiceAvailable(value = KartagoMapService.class)
   public String kartagoclientexe()
   {
      return ((KartagoPluginConfiguration) kartagoMapService.configuration()).installpath().get() ;
   }
}
