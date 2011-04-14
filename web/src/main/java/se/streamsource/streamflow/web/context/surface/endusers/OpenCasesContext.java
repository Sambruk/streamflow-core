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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

import java.util.*;

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
      queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
      queryBuilder.append( " status:OPEN" );
      queryBuilder.append( " contactId:"+endUser.toString().split("/")[1]);
      Query<Case> query = module.queryBuilderFactory()
            .newNamedQuery( Case.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" ).setVariable( "query", queryBuilder.toString() );

      // Paging
      if (tableQuery.offset() != null)
         query.firstResult(Integer.parseInt(tableQuery.offset()));
      if (tableQuery.limit() != null)
         query.maxResults(Integer.parseInt(tableQuery.limit()));

      ArrayList<Case> list = (ArrayList<Case>) Iterables.addAll(new ArrayList<Case>(), query);

      // Sort by last history message date
      Collections.sort(list, new Comparator<Case>()
      {
         public int compare(Case aCase, Case aCase1)
         {
            Message lastMessage = aCase.getHistory().getLastMessage();
            Message lastMessage1 = aCase1.getHistory().getLastMessage();

            Date caseDate = lastMessage == null ? aCase.createdOn().get() : ((Message.Data)lastMessage).createdOn().get();
            Date caseDate1 = lastMessage1 == null ? aCase1.createdOn().get() : ((Message.Data)lastMessage1).createdOn().get();

            return caseDate1.compareTo(caseDate);
         }
      });

      return list;
   }
}