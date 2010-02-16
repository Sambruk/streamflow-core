/*
* Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package se.streamsource.streamflow.web.domain.entity.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegator;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(WaitingForQueries.Mixin.class)
public interface WaitingForQueries
{
   QueryBuilder<Delegatable> waitingFor( @Optional Delegator delegator );

   boolean hasActiveOrDoneAndUnreadTasks();

   class Mixin
         implements WaitingForQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      public QueryBuilder<Delegatable> waitingFor( Delegator delegator )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         // Find all Active delegated tasks owned by this Entity and, optionally, delegated by "delegator"
         // or delegated tasks that are marked as done
         QueryBuilder<Delegatable> queryBuilder = qbf.newQueryBuilder( Delegatable.class );
         Association<Delegator> delegatedBy = templateFor( Delegatable.Data.class ).delegatedBy();
         Association<Owner> ownerAssociation = templateFor( Delegatable.Data.class ).delegatedFrom();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         queryBuilder = queryBuilder.where( and(
               eq( ownerAssociation, this.owner ),
               delegator != null ? eq( delegatedBy, delegator ) : QueryExpressions.isNotNull( delegatedBy ),
               isNotNull( delegatee ),
               or(
                     QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ),
                     eq( templateFor( Status.Data.class ).status(), States.DONE ) ) ) );

         return queryBuilder;
      }

      public boolean hasActiveOrDoneAndUnreadTasks()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         // Find all Active delegated tasks owned by this Entity
         // or Completed delegated tasks that are marked as unread
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Association<Owner> ownerAssociation = templateFor( Delegatable.Data.class ).delegatedFrom();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         Query<TaskEntity> waitingForQuery = queryBuilder.where( and(
               eq( ownerAssociation, this.owner ),
               isNotNull( delegatee ),
               or(
                     eq( templateFor( Status.Data.class ).status(), States.ACTIVE ),
                     eq( templateFor( Status.Data.class ).status(), States.DONE ) ) ) ).
               newQuery( uow );

         return waitingForQuery.count() > 0;
      }
   }
}