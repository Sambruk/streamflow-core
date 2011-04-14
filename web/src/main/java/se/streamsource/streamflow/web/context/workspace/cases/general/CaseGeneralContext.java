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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.general.CaseGeneralDTO;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;

/**
 * JAVADOC
 */
public class CaseGeneralContext
   implements IndexContext<CaseGeneralDTO>
{
   @Structure Module module;

   public CaseGeneralDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<CaseGeneralDTO> builder = vbf.newValueBuilder( CaseGeneralDTO.class );
      CaseEntity aCase = RoleMap.role( CaseEntity.class );
      builder.prototype().description().set( aCase.description().get() );

      CaseType caseType = aCase.caseType().get();
      if (caseType != null)
      {
         ValueBuilder<LinkValue> caseTypeBuilder = vbf.newValueBuilder( LinkValue.class );

         caseTypeBuilder.prototype().text().set( caseType.getDescription() );
         caseTypeBuilder.prototype().id().set( EntityReference.getEntityReference( caseType ).identity() );
         caseTypeBuilder.prototype().href().set( EntityReference.getEntityReference( caseType ).identity() );
         builder.prototype().caseType().set( caseTypeBuilder.newInstance() );
      }

      builder.prototype().note().set( aCase.note().get() );
      builder.prototype().creationDate().set( aCase.createdOn().get() );
      builder.prototype().caseId().set( aCase.caseId().get() );
      builder.prototype().dueOn().set( aCase.dueOn().get() );
      builder.prototype().status().set( aCase.status().get() );

      return builder.newInstance();
   }
}
