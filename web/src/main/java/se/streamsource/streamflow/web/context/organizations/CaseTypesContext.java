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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(CaseTypesContext.Mixin.class)
public interface CaseTypesContext
   extends SubContexts<CaseTypeContext>, IndexContext<LinksValue>, Context
{
   public void createcasetype( StringValue name );

   abstract class Mixin
      extends ContextMixin
      implements CaseTypesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         CaseTypes.Data caseTypes = roleMap.get( CaseTypes.Data.class);
         return new LinksBuilder(module.valueBuilderFactory()).rel( "casetype" ).addDescribables( caseTypes.caseTypes()).newLinks();
      }

      public void createcasetype( StringValue name )
      {
         CaseTypes caseTypes = roleMap.get( CaseTypes.class);
         caseTypes.createCaseType( name.string().get() );
      }

      public CaseTypeContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( CaseType.class, id ));

         return subContext( CaseTypeContext.class );
      }
   }
}
