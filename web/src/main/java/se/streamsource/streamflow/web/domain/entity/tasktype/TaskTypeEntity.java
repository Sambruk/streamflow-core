/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.entity.tasktype;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;

/**
 * JAVADOC
 */
@Concerns(TaskTypeEntity.RemovableConcern.class)
public interface TaskTypeEntity
      extends DomainEntity,

      // Structure
      TaskType,
      Describable.Data,
      Notable.Data,
      SelectedLabels.Data,
      Forms.Data,

      // Queries
      PossibleLabelsQueries
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      TaskType taskType;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this task-type
         if (removed)
         {
            {
               SelectedTaskTypes.Data selectedTaskTypes = QueryExpressions.templateFor( SelectedTaskTypes.Data.class );
               Query<SelectedTaskTypes> taskTypeUsages = qbf.newQueryBuilder( SelectedTaskTypes.class ).
                     where( QueryExpressions.contains(selectedTaskTypes.selectedTaskTypes(), taskType )).
                     newQuery( uowf.currentUnitOfWork() );

               for (SelectedTaskTypes taskTypeUsage : taskTypeUsages)
               {
                  taskTypeUsage.removeSelectedTaskType( taskType );
               }
            }
         }

         return removed;
      }

      public void deleteEntity()
      {
         next.deleteEntity();
      }
   }

}

