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

package se.streamsource.streamflow.web.domain.entity.tasktype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(TaskTypesQueries.Mixin.class)
public interface TaskTypesQueries
{
   // Queries
   QueryBuilder<Project> possibleProjects( @Optional TaskType taskType );

   TaskType getTaskTypeByName( String name );

   abstract class Mixin
         implements TaskTypesQueries, TaskTypes.Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      public TaskType getTaskTypeByName( String name )
      {
         return Describable.Mixin.getDescribable( taskTypes(), name );
      }

      public QueryBuilder<Project> possibleProjects( TaskType taskType )
      {
         QueryBuilder<Project> projects = qbf.newQueryBuilder( Project.class );

         SelectedTaskTypes.Data template = templateFor( SelectedTaskTypes.Data.class );

         if (taskType != null)
            projects = projects.where( contains( template.selectedTaskTypes(), taskType ) );
         else
         {
            projects = projects.where( and( eq( templateFor( Removable.Data.class ).removed(), false ),
                                            isNotNull( templateFor(OwningOrganizationalUnit.Data.class).organizationalUnit() )));
         }

         return projects;
      }
   }
}