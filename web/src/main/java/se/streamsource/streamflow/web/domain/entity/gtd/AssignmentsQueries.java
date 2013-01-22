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
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

@Mixins(AssignmentsQueries.Mixin.class)
public interface AssignmentsQueries
        extends AbstractCaseQueriesFilter
{
   QueryBuilder<Case> assignments(@Optional Assignee assignee, @Optional String filter);

   boolean assignmentsHaveActiveCases();

   abstract class Mixin
           implements AssignmentsQueries
   {
      @Structure
      Module module;

      @This
      Owner owner;

      public QueryBuilder<Case> assignments(Assignee assignee, String filter)
      {
         Association<Assignee> assignedId = templateFor(Assignable.Data.class).assignedTo();
         Association<Owner> ownedId = templateFor(Ownable.Data.class).owner();

         // Find all my OPEN cases assigned to optional assignee
         QueryBuilder<Case> openQueryBuilder = module.queryBuilderFactory().newQueryBuilder(Case.class);
         openQueryBuilder = openQueryBuilder.where(and(or(
                 eq(templateFor(Status.Data.class).status(), CaseStates.OPEN),
                 eq(templateFor(Status.Data.class).status(), CaseStates.ON_HOLD)),
                 eq( templateFor(Removable.Data.class).removed(), Boolean.FALSE ),
                 assignee == null ? isNotNull(assignedId) : eq(assignedId, assignee),
                 eq(ownedId, owner))
         );

         if (!Strings.empty(filter))
            openQueryBuilder = applyFilter(openQueryBuilder, filter);

         return openQueryBuilder;
      }

      public boolean assignmentsHaveActiveCases()
      {
         QueryBuilder<CaseEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(CaseEntity.class);
         Association<Assignee> assignedId = templateFor(Assignable.Data.class).assignedTo();
         Association<Owner> ownedId = templateFor(Ownable.Data.class).owner();
         Query<CaseEntity> assignmentsQuery = queryBuilder.where(and(
               isNotNull(assignedId),
               eq(ownedId, owner),
               or( eq(templateFor(Status.Data.class).status(), CaseStates.OPEN),
                   eq(templateFor(Status.Data.class).status(), CaseStates.ON_HOLD)),
               eq( templateFor(Removable.Data.class).removed(), Boolean.FALSE ))).
                 newQuery(module.unitOfWorkFactory().currentUnitOfWork());

         return assignmentsQuery.count() > 0;
      }

   }
}