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

package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.application.defaults.DefaultSystemConfigurationService;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountAssignmentsConcern.class)
@Mixins(AssignmentsContext.Mixin.class)
public interface AssignmentsContext
        extends AbstractFilterContext
{
   public Query<Case> cases(TableQuery tableQuery);

   public void createcase();

   abstract class Mixin
           implements AssignmentsContext
   {
      @Structure
      Module module;

      @Service
      DefaultSystemConfigurationService systemConfig;

      public Query<Case> cases(TableQuery tableQuery)
      {


         AssignmentsQueries assignments = RoleMap.role(AssignmentsQueries.class);

         QueryBuilder<Case> builder = assignments.assignments(RoleMap.role(Assignee.class), tableQuery.where());

         Query<Case> query = builder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).orderBy(orderBy(templateFor(CreatedOn.class).createdOn(), OrderBy.Order.DESCENDING));

         if( systemConfig.config().configuration().sortOrderAscending().get())
         {
            query.orderBy( orderBy( templateFor(CreatedOn.class).createdOn(), OrderBy.Order.ASCENDING) );
         }

         // Paging
         if (tableQuery.offset() != null)
            query.firstResult(Integer.parseInt(tableQuery.offset()));
         if (tableQuery.limit() != null)
            query.maxResults(Integer.parseInt(tableQuery.limit()));

         if (tableQuery.orderBy() != null)
         {
            String[] orderByValue = tableQuery.orderBy().split(" ");
            OrderBy.Order order = orderByValue[1].equals("asc") ? OrderBy.Order.ASCENDING : OrderBy.Order.DESCENDING;

            if (tableQuery.orderBy().equals("status"))
            {
               query.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(Status.Data.class).status(), order));
            } else if (orderByValue[0].equals("description"))
            {
               query.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(Describable.Data.class).description(), order));
            } else if (orderByValue[0].equals("dueOn"))
            {
               query.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(DueOn.Data.class).dueOn(), order));
            } else if (orderByValue[0].equals("createdOn"))
            {
               query.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(CreatedOn.class).createdOn(), order));
            }
         }
         return query;
      }

      public void createcase()
      {
         Drafts drafts = RoleMap.role(Drafts.class);
         CaseEntity caze = drafts.createDraft();

         Owner owner = RoleMap.role(Owner.class);
         caze.changeOwner(owner);

         caze.open();

         caze.assignTo(RoleMap.role(Assignee.class));
      }
   }
}