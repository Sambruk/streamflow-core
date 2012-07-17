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
package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriority;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;
import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountDraftsConcern.class)
@Mixins(DraftsContext.Mixin.class)
public interface DraftsContext
        extends Context
{
   Query<Case> cases(TableQuery tableQuery);

   void createcase();

   Query<LabelEntity> possibleLabels();

   Query<CaseTypeEntity> possibleCaseTypes();

   LinksValue possibleProjects();

   LinksValue priorities();

   abstract class Mixin
           implements DraftsContext
   {
      @Structure
      Module module;

      @Service
      SystemDefaultsService systemConfig;

      public Query<Case> cases(TableQuery tableQuery)
      {
         DraftsQueries inbox = role(DraftsQueries.class);

         QueryBuilder<Case> builder = inbox.drafts(tableQuery.where());

         Query<Case> query = builder.newQuery(module.unitOfWorkFactory().currentUnitOfWork())
               .orderBy( orderBy( templateFor( CreatedOn.class ).createdOn(), OrderBy.Order.DESCENDING ) );

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
            }else if( orderByValue[0].equals( "priority" ))
            {
               query.orderBy(  QueryExpressions.orderBy(
                     QueryExpressions.templateFor( PrioritySettings.Data.class, QueryExpressions.templateFor( CasePriority.Data.class ).priority().get() ).priority(), revertSortOrder( order ) ) );
            }
         }
         return query;
      }

      public void createcase()
      {
         Drafts drafts = role(Drafts.class);
         drafts.createDraft();
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

      public LinksValue priorities()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         RoleMap.current().set( org );

         Priorities.Data priorities = RoleMap.role( Priorities.Data.class );
         se.streamsource.streamflow.web.context.LinksBuilder builder = new se.streamsource.streamflow.web.context.LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<PriorityValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( PriorityValue.class );

         List<Priority> sortedList =  priorities.prioritys().toList();
         Collections.sort( sortedList, new Comparator<Priority>()
         {
            public int compare( Priority o1, Priority o2 )
            {
               return ((PrioritySettings.Data) o1).priority().get().compareTo( ((PrioritySettings.Data) o2).priority().get() );
            }
         } );

         for(Priority priority : sortedList )
         {
            linkBuilder.prototype().id().set( EntityReference.getEntityReference( priority ).identity() );
            linkBuilder.prototype().color().set( ((PrioritySettings.Data)priority).color().get() );
            linkBuilder.prototype().priority().set( ((PrioritySettings.Data)priority).priority().get() );
            linkBuilder.prototype().href().set( "na" );
            linkBuilder.prototype().text().set( priority.getDescription() );
            builder.addLink( linkBuilder.newInstance() );
         }
         return builder.newLinks();
      }

      public OrderBy.Order revertSortOrder( OrderBy.Order order )
      {
         if( OrderBy.Order.ASCENDING.equals( order ))
            return OrderBy.Order.DESCENDING;
         else
            return OrderBy.Order.ASCENDING;
      }

   }
}
