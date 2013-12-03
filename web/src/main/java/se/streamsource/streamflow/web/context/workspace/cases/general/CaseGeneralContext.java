/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.api.workspace.cases.general.CaseGeneralDTO;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;

/**
 * JAVADOC
 */
public class CaseGeneralContext
   implements IndexContext<CaseGeneralDTO>
{
   @Structure Module module;

   @Service
   KnowledgebaseService knowledgebaseService;

   public CaseGeneralDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<CaseGeneralDTO> builder = vbf.newValueBuilder( CaseGeneralDTO.class );
      CaseEntity aCase = RoleMap.role( CaseEntity.class );
      // STREAMFLOW-714
      // There might be cases created with desciption longer than 50 characters. CaseGeneralDTO only allows 50 så
      // we have to make sure we only let 50 chars through to avoid Nullpointer on the klient side.
      // The real fix for this is in CreateCaseFromEmail
      String description = aCase.description().get();
      builder.prototype().description().set( description.length() > 50 ? description.substring( 0, 50 ) : description );

      CaseType caseType = aCase.caseType().get();
      if (caseType != null)
      {
         ValueBuilder<LinkValue> caseTypeBuilder = vbf.newValueBuilder( LinkValue.class );

         caseTypeBuilder.prototype().text().set( caseType.getDescription() );
         caseTypeBuilder.prototype().id().set( EntityReference.getEntityReference( caseType ).identity() );
         caseTypeBuilder.prototype().href().set(knowledgebaseService.createURL((EntityComposite) caseType));
         builder.prototype().caseType().set( caseTypeBuilder.newInstance() );
      }

      builder.prototype().creationDate().set( aCase.createdOn().get() );
      builder.prototype().caseId().set( aCase.caseId().get() );
      builder.prototype().dueOn().set( aCase.dueOn().get() );
      builder.prototype().status().set( aCase.status().get() );

      if( aCase.priority().get() != null )
      {
         ValueBuilder<LinkValue> priorityBuilder = vbf.newValueBuilder( LinkValue.class );
         priorityBuilder.prototype().text().set( aCase.priority().get().getDescription() );
         priorityBuilder.prototype().id().set( EntityReference.getEntityReference( aCase.priority().get() ).identity() );
         priorityBuilder.prototype().href().set( "default" );
         builder.prototype().priority().set( priorityBuilder.newInstance() );
      }

      return builder.newInstance();
   }
   
   public LinksValue caselog()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<CaseLogEntryDTO> builder = module.valueBuilderFactory().newValueBuilder( CaseLogEntryDTO.class );

      
      return links.newLinks();
   }
}
