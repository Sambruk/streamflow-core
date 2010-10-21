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

package se.streamsource.streamflow.web.domain.entity.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

import static org.qi4j.api.query.QueryExpressions.*;
import static se.streamsource.streamflow.util.Iterables.*;

@Mixins(AssignmentsQueries.Mixin.class)
public interface AssignmentsQueries
{
   Query<Case> assignments(@Optional Assignee assignee);

   boolean assignmentsHaveActiveCases();

   class Mixin
         implements AssignmentsQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      public Query<Case> assignments( Assignee assignee )
      {
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();

         // Find all my OPEN cases assigned to optional assignee
         QueryBuilder<Case> openQueryBuilder = qbf.newQueryBuilder( Case.class );
         openQueryBuilder = openQueryBuilder.where( and(
               eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ),
               assignee == null ? isNotNull( assignedId ) : eq( assignedId, assignee ),
               eq( ownedId, owner ))
               );

         // Find all my ON_HOLD cases assigned to optional assignee
         QueryBuilder<Case> onHoldQueryBuilder = qbf.newQueryBuilder( Case.class );
         onHoldQueryBuilder = onHoldQueryBuilder.where( and(
               eq( templateFor( Status.Data.class ).status(), CaseStates.ON_HOLD ),
               assignee == null ? isNotNull( assignedId ) : eq( assignedId, assignee ),
               eq( ownedId, owner ))
               );


         Iterable<Case> assignables = flatten(openQueryBuilder.newQuery(uowf.currentUnitOfWork() ), onHoldQueryBuilder.newQuery( uowf.currentUnitOfWork() ));
         return qbf.newQueryBuilder( Case.class ).newQuery( assignables );
      }

      public boolean assignmentsHaveActiveCases()
      {
         QueryBuilder<CaseEntity> queryBuilder = qbf.newQueryBuilder( CaseEntity.class );
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();
         Query<CaseEntity> assignmentsQuery = queryBuilder.where( and(
               isNotNull( assignedId ),
               eq( ownedId, owner ),
               eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ) ) ).
               newQuery( uowf.currentUnitOfWork() );

         return assignmentsQuery.count() > 0;
      }

   }
}