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

package se.streamsource.streamflow.web.context.surface.endusers;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.query.*;
import org.qi4j.api.structure.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

/**
 * JAVADOC
 */
public class ClosedCasesContext
{
   @Structure
   Module module;

   public Query<Case> cases(TableQuery tableQuery)
   {
      EndUser endUser = RoleMap.role(EndUser.class);

      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
      queryBuilder.append( " status:CLOSED" );
      queryBuilder.append( " contactId:"+endUser.toString().split("/")[1]);
      Query<Case> query = module.queryBuilderFactory()
            .newNamedQuery( Case.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" ).setVariable( "query", queryBuilder.toString() );

      // Paging
      if (tableQuery.offset() != null)
         query.firstResult(Integer.parseInt(tableQuery.offset()));
      if (tableQuery.limit() != null)
         query.maxResults(Integer.parseInt(tableQuery.limit()));

      return query;
   }
}