/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.SearchCaseQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.dci.api.SubContexts;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(CasesContext.Mixin.class)
public interface CasesContext
   extends SubContexts<CaseContext>, Interactions
{
   LinksValue search( StringValue query);

   abstract class Mixin
      extends InteractionsMixin
      implements CasesContext
   {
      public static LinksValue buildCaseList(Iterable<Case> query, Module module, String basePath)
      {
         LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory()).path( basePath );
         for (Case aCase : query)
         {
            linksBuilder.addLink( CaseContext.Mixin.caseDTO( (CaseEntity) aCase, module, basePath ));
         }
         return linksBuilder.newLinks();
      }

      @Structure
      Module module;

      public LinksValue search( StringValue query )
      {
         SearchCaseQueries caseQueries = context.get( SearchCaseQueries.class );
         String name = context.get( UserAuthentication.Data.class ).userName().get();
         Query<Case> caseQuery = caseQueries.search( query, name );
         caseQuery.orderBy( QueryExpressions.orderBy(QueryExpressions.templateFor( Describable.Data.class ).description()) );

         List<Case> filteredCases = new ArrayList<Case>( );
         for (Case aCase : caseQuery)
         {
            if (((Ownable)aCase).hasOwner())
               filteredCases.add( aCase );
         }

         return buildCaseList( filteredCases, module, context.get(Reference.class).getBaseRef().getPath());
      }

      public CaseContext context( String id )
      {
         CaseEntity aCase = module.unitOfWorkFactory().currentUnitOfWork().get( CaseEntity.class, id );
         context.set( aCase );

         return subContext( CaseContext.class);
      }
   }
}
