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

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.Date;

/**
 * JAVADOC
 */
@Mixins(CaseStatisticsService.Mixin.class)
public interface CaseStatisticsService
   extends ServiceComposite, Activatable, CaseStatistics
{
   class Mixin
      implements TransactionVisitor, Activatable, CaseStatistics
   {
      @Service
      EventStore eventStore;

      @Service
      EventSource source;

      @Service
      EntityStore entityStore;

      @Service
      Iterable<StatisticsStore> statisticsStores;

      @Structure
      ModuleSPI module;

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
      }

      public void refresh()
      {
         // First clear the statistics stores of all their existing data
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.clearAll();
         }

         // Update all related entities
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            try
            {
               // Users
               Query<User> users = module.queryBuilderFactory().newQueryBuilder( User.class ).newQuery( uow );
               ValueBuilder<RelatedStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder( RelatedStatisticsValue.class );
               for (User user : users)
               {
                  builder.prototype().identity().set( ((Identity)user).identity().get());
                  builder.prototype().description().set( user.getDescription() );
                  builder.prototype().type().set( RelatedEnum.user );

                  notifyStores(builder.newInstance());
               }
            } finally
            {
               uow.discard();
            }
         }

         // Update all case statistics
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            QueryBuilder<CaseEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( CaseEntity.class );
            Query<CaseEntity> cases = queryBuilder.where( QueryExpressions.eq(QueryExpressions.templateFor( Status.Data.class ).status(), CaseStates.CLOSED)).newQuery( uow );
            for (CaseEntity aCase : cases)
            {
               update(aCase);
            }
         }
         
      }

      public boolean visit( TransactionEvents transaction )
      {
         return true;
      }

      private void update( CaseEntity aCase)
      {
         ValueBuilder<CaseStatisticsValue> builder = module.valueBuilderFactory().newValueBuilder( CaseStatisticsValue.class );
         CaseStatisticsValue prototype = builder.prototype();

         prototype.identity().set( aCase.identity().get() );
         prototype.description().set( aCase.getDescription() );
         prototype.note().set( aCase.note().get() );
         prototype.assigneeId().set( ((Identity)aCase.assignedTo().get()).identity().get() );
         prototype.caseId().set( aCase.caseId().get() );
         prototype.closedOn().set( new Date(aCase.createdOn().get().getTime()) );
         prototype.closedOn().set( new Date(aCase.createdOn().get().getTime()) );

         CaseType caseType = aCase.caseType().get();
         if (caseType != null)
         {
            UnitOfWork uow = module.unitOfWorkFactory().getUnitOfWork( aCase );
            prototype.caseTypeId().set( ((Identity) caseType).identity().get() );

            QueryBuilder<Identity> caseOwnerQuery = module.queryBuilderFactory().newQueryBuilder( Identity.class );
            ManyAssociation<CaseType> caseTypes = QueryExpressions.templateFor( CaseTypes.Data.class ).caseTypes();
            Identity caseTypeOwner = caseOwnerQuery.where( QueryExpressions.contains( caseTypes, caseType ) ).newQuery( uow ).find();

            prototype.caseTypeOwnerId().set( caseTypeOwner.identity().get() );
         }



         notifyStores(builder.newInstance());
      }

      private void notifyStores( RelatedStatisticsValue relatedStatisticsValue )
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.related( relatedStatisticsValue );
         }
      }

      private void notifyStores( CaseStatisticsValue caseStatisticsValue )
      {
         for (StatisticsStore statisticsStore : statisticsStores)
         {
            statisticsStore.caseStatistics( caseStatisticsValue );
         }
      }
   }
}
