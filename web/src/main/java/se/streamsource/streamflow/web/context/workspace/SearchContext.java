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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.SearchCaseQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
public class SearchContext
{
   @Structure
   Module module;

   public Iterable<Case> cases(TableQuery tableQuery)
   {
      SearchCaseQueries caseQueries = RoleMap.role(SearchCaseQueries.class);
      Query<Case> caseQuery = caseQueries.search(tableQuery.where());

      caseQuery = module.queryBuilderFactory().newQueryBuilder(Case.class).newQuery(caseQuery).orderBy(orderBy(templateFor(CreatedOn.class).createdOn()));

      // Paging
      if (tableQuery.offset() != null)
         caseQuery.firstResult(Integer.parseInt(tableQuery.offset()));
      if (tableQuery.limit() != null)
         caseQuery.maxResults(Integer.parseInt(tableQuery.limit()));
      if (tableQuery.orderBy() != null)
      {
         String[] orderByValue = tableQuery.orderBy().split(" ");
         Order order = orderByValue[1].equals("asc") ? Order.ASCENDING : Order.DESCENDING;

         if (tableQuery.orderBy().equals("status"))
         {
            caseQuery.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(Status.Data.class).status(), order));
         } else if (orderByValue[0].equals("description"))
         {
            caseQuery.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(Describable.Data.class).description(), order));
         } else if (orderByValue[0].equals("dueOn"))
         {
            caseQuery.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(DueOn.Data.class).dueOn(), order));
         } else if (orderByValue[0].equals("createdOn"))
         {
            caseQuery.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(CreatedOn.class).createdOn(), order));
         }
      }
      return caseQuery;
   }

   public Query<LabelEntity> possibleLabels()
   {
      QueryBuilder<LabelEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(LabelEntity.class);
      queryBuilder = queryBuilder.where(
              eq(templateFor(Removable.Data.class).removed(), false));
      return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
              orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
   }

   public Query<CaseTypeEntity> possibleCaseTypes()
   {
      QueryBuilder<CaseTypeEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(CaseTypeEntity.class);
      queryBuilder = queryBuilder.where(
              eq(templateFor(Removable.Data.class).removed(), false));
      return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
              orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
   }

   public Query<UserEntity> possibleAssignees()
   {
      QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(UserEntity.class);
      queryBuilder = queryBuilder.where(
              eq(templateFor(UserAuthentication.Data.class).disabled(), false));
      return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
              orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
   }

   public LinksValue possibleProjects()
   {
      QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(ProjectEntity.class);
      queryBuilder = queryBuilder.where(
              eq(templateFor(Removable.Data.class).removed(), false));
      Query<ProjectEntity> query = queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
              orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());

      for (ProjectEntity project : query)
      {
         linksBuilder.addLink(project.getDescription(), project.identity().get(), "", "", "", ((Describable) ((Ownable.Data) project).owner().get()).getDescription());
      }
      return linksBuilder.newLinks();
   }

   public Query<UserEntity> possibleCreatedBy()
   {
      QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(UserEntity.class);
      return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
              orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
   }

   /**
    * Convenience method to be able to tell the gui that Status has to be rendered visible in the Perspective filter.
    *
    * @return
    */
   public LinksValue possibleStatus()
   {
      return new LinksBuilder(module.valueBuilderFactory()).newLinks();
   }
}
