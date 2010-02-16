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

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(DelegationsQueries.Mixin.class)
public interface DelegationsQueries
{
   QueryBuilder<Delegatable> delegations();

   class Mixin
         implements DelegationsQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Delegatee delegatee;

      public QueryBuilder<Delegatable> delegations()
      {
         // Find all Active tasks delegated to "me"
         QueryBuilder<Delegatable> queryBuilder = qbf.newQueryBuilder( Delegatable.class );
         Association<Delegatee> delegatedTo = templateFor( Delegatable.Data.class ).delegatedTo();
         Association<Assignee> assignee = templateFor( Assignable.Data.class ).assignedTo();
         queryBuilder = queryBuilder.where( and(
               eq( delegatedTo, delegatee ),
               isNull( assignee ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ) ) );

         return queryBuilder;
      }

   }
}