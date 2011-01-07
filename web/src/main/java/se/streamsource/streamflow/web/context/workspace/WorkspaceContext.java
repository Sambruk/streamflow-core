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

package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.SearchCaseQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
public class WorkspaceContext
{
   @Structure
   Module module;

   public Iterable<Case> search( TableQuery tableQuery)
   {
      SearchCaseQueries caseQueries = RoleMap.role( SearchCaseQueries.class );
      Query<Case> caseQuery = caseQueries.search( tableQuery.where() );

      return caseQuery.maxResults( 1000 );
   }

   public Query<LabelEntity> possibleLabels()
   {
      QueryBuilder<LabelEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( LabelEntity.class );
      queryBuilder = queryBuilder.where(
            eq( templateFor( Removable.Data.class ).removed(), false ));
      return queryBuilder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).
            orderBy( QueryExpressions.orderBy( templateFor( Describable.Data.class).description() ) );
   }

   public Query<UserEntity> possibleAssignees()
   {
      QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( UserEntity.class );
      queryBuilder = queryBuilder.where(
                  eq( templateFor( UserAuthentication.Data.class ).disabled(), false ));
      return queryBuilder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).
         orderBy( QueryExpressions.orderBy( templateFor( Describable.Data.class).description() ) );
   }

   public Query<ProjectEntity> possibleProjects()
   {
      QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class );
      queryBuilder = queryBuilder.where(
                  eq( templateFor( Removable.Data.class ).removed(), false ));
      return queryBuilder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).
            orderBy( QueryExpressions.orderBy( templateFor( Describable.Data.class).description() ) );
   }
}
