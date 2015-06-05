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
package se.streamsource.streamflow.web.context.util;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.gdq.OrderByDirection;
import se.streamsource.dci.value.table.gdq.OrderByElement;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriority;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

/** Converter for converting a Query<Case> by applying query arguments such as ordering
 * and pagination from a TableQuery object.
 */
public class TableQueryConverter {

   private TableQuery tableQuery;

   @Structure
   Module module;

   @Service
   SystemDefaultsService systemConfig;

   public TableQueryConverter(@Uses TableQuery tableQuery) {
      this.tableQuery = tableQuery;
   }

   public Query<Case> convert(Query<Case> originalQuery) {

      Query<Case> caseQuery = module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( originalQuery )
            .orderBy( orderBy( templateFor( CreatedOn.class ).createdOn(), OrderBy.Order.DESCENDING ) );

      if( systemConfig.config().configuration().sortOrderAscending().get())
      {
         caseQuery.orderBy( orderBy( templateFor(CreatedOn.class).createdOn(), OrderBy.Order.ASCENDING) );
      }

      // Paging
      if (tableQuery.offset() != null)
         caseQuery.firstResult(tableQuery.offset());
      if (tableQuery.limit() != null)
         caseQuery.maxResults(tableQuery.limit());
      if (tableQuery.orderBy() != null && tableQuery.orderBy().size()>0) {
         caseQuery.orderBy(tableQueryToOrderByArray(tableQuery.orderBy()));
      }

      return caseQuery;
   }

   private static OrderBy[] tableQueryToOrderByArray(List<OrderByElement> orderByList) {
      ArrayList<OrderBy> orderBys = new ArrayList<OrderBy>();
      for (OrderByElement element: orderByList) {
         orderBys.add(orderByElementToOrderBy(element));
      }
      return orderBys.toArray(new OrderBy[0]);
   }

   private static OrderBy orderByElementToOrderBy(OrderByElement element) {
      Order order = element.direction == OrderByDirection.DESCENDING ? Order.DESCENDING : Order.ASCENDING;
      if (element.name.equals("status")) {
         return QueryExpressions.orderBy(QueryExpressions.templateFor(Status.Data.class).status(), order);
      }
      else if (element.name.equals("description")) {
         return QueryExpressions.orderBy(QueryExpressions.templateFor(Describable.Data.class).description(), order);
      }
      else if (element.name.equals("dueOn")) {
         return QueryExpressions.orderBy(QueryExpressions.templateFor(DueOn.Data.class).dueOn(), order);
      }
      else if (element.name.equals("createdOn")) {
         return QueryExpressions.orderBy(QueryExpressions.templateFor(CreatedOn.class).createdOn(), order);
      }
      else if (element.name.equals("caseType")) {
         return QueryExpressions.orderBy(
               QueryExpressions.templateFor(Describable.Data.class, QueryExpressions.templateFor(TypedCase.Data.class).caseType().get()).description(), order);
      }
      else if (element.name.equals( "priority" )) {
         return QueryExpressions.orderBy(
               QueryExpressions.templateFor( PrioritySettings.Data.class, QueryExpressions.templateFor( CasePriority.Data.class ).casepriority().get() ).priority(), revertSortOrder( order ) );
      }
      else {
         throw new IllegalArgumentException("Order by '"+element.name+"' not supported");
      }
   }

   private static OrderBy.Order revertSortOrder( OrderBy.Order order )
   {
      if( OrderBy.Order.ASCENDING.equals( order ))
         return OrderBy.Order.DESCENDING;
      else
         return OrderBy.Order.ASCENDING;
   }

}
