/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.workspace;

import static se.streamsource.dci.api.RoleMap.role;

import java.util.Collections;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.api.workspace.PerspectiveDTO;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.Perspective;
import se.streamsource.streamflow.web.domain.structure.user.Perspectives;

/**
 * JAVADOC
 */
public class PerspectiveContext
        implements DeleteContext, IndexContext<PerspectiveDTO>
{
   public void delete()
   {
      role(Perspectives.class).removePerspective(RoleMap.role(Perspective.class));
   }

   public Iterable<Case> cases(TableQuery tableQuery)
   {
      //TODO Should delegate to corresponding context
      // Might be necessary to build up where clause including perspective filter before delegation

      /*SearchCaseQueries caseQueries = RoleMap.role( SearchCaseQueries.class );
      Perspective.Data perspective = RoleMap.role( Perspective.Data.class );
      
      Query<Case> caseQuery = caseQueries.search( perspective.perspective().get().query().get() );

      // Paging
      if (tableQuery.offset() != null)
         caseQuery.firstResult( Integer.parseInt( tableQuery.offset()) );
      if (tableQuery.limit() != null)
         caseQuery.maxResults( Integer.parseInt( tableQuery.limit()) );

      return caseQuery; */
      return Collections.<Case>emptyList();
   }

   public PerspectiveDTO index()
   {
      return role(Perspective.Data.class).perspective().get();
   }
}
