/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.api.SkipResourceValidityCheck;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.KnownDatatypeDefinitionUrls;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionPluginDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldValueDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftSettingsDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.infrastructure.plugin.KartagoPluginConfiguration;
import se.streamsource.streamflow.web.infrastructure.plugin.address.StreetAddressLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;

import java.io.IOException;
import java.util.List;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class CaseFormDraftContext implements DeleteContext, IndexContext<FormDraftDTO>
{

   @Optional
   @Service
   KartagoMapService kartagoMapService;

   @Optional
   @Service
   ServiceReference<StreetAddressLookupService> streetLookup;
   
   @Structure
   Module module;
   
   @Service
   SystemDefaultsService systemDefaults;

   public FormDraftDTO index()
   {
      FormDraft formDraft = role( FormDraft.class );

      FormDraftDTO draft = formDraft.getFormDraftValue();
      ValueBuilder<FormDraftDTO> draftBuilder = module.valueBuilderFactory().newValueBuilder( FormDraftDTO.class ).withPrototype( draft );

      boolean visibilityrule = false;

      int pageIndex = -1;
      for (PageSubmissionDTO pageSubmissionDTO : draft.pages().get())
      {
         pageIndex++;
         if ( pageSubmissionDTO.rule().get() != null && !Strings.empty( pageSubmissionDTO.rule().get().field().get() ) )
         {
            visibilityrule = true;
         }
         
         int fieldIndex = -1;
         for (FieldSubmissionDTO field : pageSubmissionDTO.fields().get())
         {
            fieldIndex++;
            if ( field.field().get().rule().get() != null && !Strings.empty( field.field().get().rule().get().field().get() ) )
            {
               visibilityrule = true;
            }
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

      draftBuilder.prototype().visibilityrules().set( visibilityrule );
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

   @ServiceAvailable( service = KartagoMapService.class, availability = true )
   public String kartagoclientexe()
   {
      return ((KartagoPluginConfiguration) kartagoMapService.configuration()).installpath().get() ;
   }

   @ServiceAvailable( service = StreetAddressLookupService.class, availability = true )
   @SkipResourceValidityCheck
   public StreetsDTO searchstreets(StreetSearchDTO search)
   {

      ValueBuilder<StreetValue> builder = module.valueBuilderFactory().newValueBuilder(StreetValue.class);
      builder.prototype().address().set( search.address().get() );
      ValueBuilder<StreetsDTO> resultBuilder = module.valueBuilderFactory().newValueBuilder( StreetsDTO.class );
      try
      {
         if (streetLookup != null)
         {
            StreetAddressLookupService lookup = streetLookup.get();
            StreetList streetList = lookup.lookup( builder.newInstance() );
            List<StreetSearchDTO> streets = resultBuilder.prototype().streets().get();
            
            for (StreetValue street : streetList.streets().get())
            {
               streets.add( module.valueBuilderFactory().newValueFromJSON( StreetSearchDTO.class, street.toJSON() ) );
            }
            return resultBuilder.newInstance();
         } else
         {
            return resultBuilder.newInstance();
         }
      } catch (ServiceImporterException e)
      {
         // Not available at this time
         return resultBuilder.newInstance();
      }
   }
   
   public FormDraftSettingsDTO settings() 
   {
       ValueBuilder<FormDraftSettingsDTO> builder = module.valueBuilderFactory().newValueBuilder( FormDraftSettingsDTO.class );
       
       builder.prototype().location().set( systemDefaults.config().configuration().mapDefaultStartLocation().get() );
       builder.prototype().zoomLevel().set( systemDefaults.config().configuration().mapDefaultZoomLevel().get() );
       
       return builder.newInstance();
   }
}
