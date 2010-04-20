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
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(InboxQueries.Mixin.class)
public interface InboxQueries
{
   QueryBuilder<Case> inbox();

   boolean inboxHasActiveCases();

   class Mixin
         implements InboxQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      @This
      Inbox.Data inbox;

      public QueryBuilder<Case> inbox()
      {
         // Find all Active cases with specific owner which have not yet been assigned
         QueryBuilder<Case> queryBuilder = qbf.newQueryBuilder( Case.class );
         Association<Owner> ownableId = templateFor( Ownable.Data.class ).owner();
         Association<Assignee> assignee = templateFor( Assignable.Data.class ).assignedTo();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         queryBuilder = queryBuilder.where( and(
               eq( ownableId, owner ),
               isNull( assignee ),
               isNull( delegatee ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ) ) );
         return queryBuilder;
      }

      public boolean inboxHasActiveCases()
      {
         return inbox().newQuery( uowf.currentUnitOfWork() ).count() > 0;
      }

   }
}
