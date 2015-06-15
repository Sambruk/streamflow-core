/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.overview;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.context.util.TableQueryConverter;
import se.streamsource.streamflow.web.context.workspace.AbstractFilterContext;
import se.streamsource.streamflow.web.context.workspace.CaseSearchResult;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

/**
 * JAVADOC
 */
@Mixins(OverviewProjectAssignmentsContext.Mixin.class)
public interface OverviewProjectAssignmentsContext
      extends AbstractFilterContext
{
   public CaseSearchResult cases( TableQuery tableQuery );

   abstract class Mixin
         implements OverviewProjectAssignmentsContext
   {
      @Structure
      Module module;

      @Service
      SystemDefaultsService systemConfig;

      public CaseSearchResult cases( TableQuery tableQuery )
      {
         AssignmentsQueries assignmentsQueries = RoleMap.role( AssignmentsQueries.class );
         Query<Case> query = assignmentsQueries.assignments( null, tableQuery.where() ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         TableQueryConverter tableQueryConverter = module.objectBuilderFactory().newObjectBuilder(TableQueryConverter.class).use(tableQuery).newInstance();
         Query<Case> convertedQuery = tableQueryConverter.convert(query);
         return new CaseSearchResult(convertedQuery);
      }
   }
}