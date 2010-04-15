/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(AssignmentsQueries.Mixin.class)
public interface AssignmentsQueries
{
   QueryBuilder<Assignable> assignments(@Optional Assignee assignee);

   boolean assignmentsHaveActiveTasks();

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

      public QueryBuilder<Assignable> assignments( Assignee assignee )
      {
         // Find all my Active tasks assigned to optional assignee
         QueryBuilder<Assignable> queryBuilder = qbf.newQueryBuilder( Assignable.class );
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();
         queryBuilder = queryBuilder.where( and(
               assignee == null ? isNotNull( assignedId ) : eq( assignedId, assignee ),
               eq( ownedId, owner ),
               QueryExpressions.or(QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ),
                                   QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.DELEGATED ), 
                                   QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ON_HOLD ) )));
         return queryBuilder;
      }

      public boolean assignmentsHaveActiveTasks()
      {
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();
         Query<TaskEntity> assignmentsQuery = queryBuilder.where( and(
               isNotNull( assignedId ),
               eq( ownedId, owner ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ) ) ).
               newQuery( uowf.currentUnitOfWork() );

         return assignmentsQuery.count() > 0;
      }

   }
}