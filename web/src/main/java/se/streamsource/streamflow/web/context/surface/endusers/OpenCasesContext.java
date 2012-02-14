/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

/**
 * JAVADOC
 */
public class OpenCasesContext
{
   @Structure
   Module module;

   public Iterable<Case> cases(TableQuery tableQuery)
   {
      EndUser endUser = RoleMap.role(EndUser.class);

      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append(" type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity");
      queryBuilder.append(" status:OPEN");
      queryBuilder.append(" contactId:" + endUser.toString().split("/")[1]);
      Query<Case> query = module.queryBuilderFactory()
              .newNamedQuery(Case.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery").setVariable("query", queryBuilder.toString());

      return query;
   }
}