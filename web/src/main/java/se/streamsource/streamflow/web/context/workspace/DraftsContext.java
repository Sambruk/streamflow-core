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
package se.streamsource.streamflow.web.context.workspace;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static se.streamsource.dci.api.RoleMap.role;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import se.streamsource.streamflow.web.context.util.TableQueryConverter;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

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

   LinksValue possibleLabels();

   LinksValue possibleCaseTypes();

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
         Query<Case> query = inbox.drafts(tableQuery.where()).newQuery(module.unitOfWorkFactory().currentUnitOfWork());

         TableQueryConverter tableQueryConverter = module.objectBuilderFactory().newObjectBuilder(TableQueryConverter.class).use(tableQuery).newInstance();
         Query<Case> convertedQuery = tableQueryConverter.convert(query);
         return convertedQuery;
      }

      public void createcase()
      {
         Drafts drafts = role(Drafts.class);
         drafts.createDraft();
      }

      public LinksValue possibleLabels()
      {
          Query<Labels> labelsList = module.queryBuilderFactory().newQueryBuilder( Labels.class )
                  .where( QueryExpressions.eq( QueryExpressions.templateFor( Removable.Data.class ).removed(), false ) )
                  .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

          se.streamsource.streamflow.web.context.LinksBuilder builder = new se.streamsource.streamflow.web.context.LinksBuilder( module.valueBuilderFactory() );

          for( Labels labels : labelsList )
          {
              for( Label label : ((Labels.Data)labels).labels() )
              {
                  builder.addDescribable( (Describable) label, ((Describable) labels).getDescription() );
              }
          }
          return builder.newLinks();
          /*
         QueryBuilder<LabelEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(LabelEntity.class);
         queryBuilder = queryBuilder.where(
                 eq(templateFor(Removable.Data.class).removed(), false));
         return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
                 orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
                 */
      }

      public LinksValue possibleCaseTypes()
      {
          QueryBuilder<CaseTypeEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(CaseTypeEntity.class);
          queryBuilder = queryBuilder.where(
                  eq(templateFor(Removable.Data.class).removed(), false));
          Query<CaseTypeEntity> query = queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
                  orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));

          se.streamsource.streamflow.web.context.LinksBuilder linksBuilder = new se.streamsource.streamflow.web.context.LinksBuilder( module.valueBuilderFactory() );

          for( CaseTypeEntity caseTypeEntity : query )
          {
              Owner owner = ((Ownable.Data)caseTypeEntity).owner().get();

              String title = owner != null ? ((Describable)owner).getDescription() : "";
              linksBuilder.addLink( caseTypeEntity.getDescription(), caseTypeEntity.identity().get(),"","","", title );
          }
          return linksBuilder.newLinks();
          /*
         QueryBuilder<CaseTypeEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(CaseTypeEntity.class);
         queryBuilder = queryBuilder.where(
                 eq(templateFor(Removable.Data.class).removed(), false));
         return queryBuilder.newQuery(module.unitOfWorkFactory().currentUnitOfWork()).
                 orderBy(QueryExpressions.orderBy(templateFor(Describable.Data.class).description()));
                 */
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
