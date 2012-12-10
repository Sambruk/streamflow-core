/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.entity.gtd;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

@Mixins(DraftsQueries.Mixin.class)
public interface DraftsQueries
        extends AbstractCaseQueriesFilter
{
   QueryBuilder<Case> drafts(@Optional String filter);

   abstract class Mixin
           implements DraftsQueries
   {
      @Structure
      Module module;

      @This
      Creator creator;

      public QueryBuilder<Case> drafts(String filter)
      {
         // Find all Draft cases with specific creator which have not yet been opened
         QueryBuilder<Case> queryBuilder = module.queryBuilderFactory().newQueryBuilder(Case.class);
         Association<Creator> createdId = templateFor(CreatedOn.class).createdBy();
         queryBuilder = queryBuilder.where(and(
                 eq(createdId, creator),
                 QueryExpressions.eq(templateFor(Status.Data.class).status(), CaseStates.DRAFT)));

         if (!Strings.empty(filter))
            queryBuilder = applyFilter(queryBuilder, filter);

         return queryBuilder;
      }
   }
}