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
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(DraftsQueries.Mixin.class)
public interface DraftsQueries
{
   QueryBuilder<Case> drafts();

   class Mixin
         implements DraftsQueries
   {
      @Structure
      QueryBuilderFactory qbf;

      @This
      Creator creator;

      public QueryBuilder<Case> drafts()
      {
         // Find all Draft cases with specific creator which have not yet been opened
         QueryBuilder<Case> queryBuilder = qbf.newQueryBuilder( Case.class );
         Association<Creator> createdId = templateFor( CreatedOn.class ).createdBy();
         queryBuilder = queryBuilder.where( and(
               eq( createdId, creator ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), CaseStates.DRAFT ) ) );
         return queryBuilder;
      }
   }
}