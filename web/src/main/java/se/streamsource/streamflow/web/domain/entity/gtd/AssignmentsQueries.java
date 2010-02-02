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

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import java.util.List;

@Mixins(AssignmentsQueries.Mixin.class)
public interface AssignmentsQueries
{
   Query<Assignable> assignments(@Optional Assignee assignee);

   TaskListDTO assignmentsTasks( @Optional Assignee assignee );

   boolean assignmentsHaveActiveTasks();

   class Mixin
         implements AssignmentsQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      public Query<Assignable> assignments( Assignee assignee )
      {
         // Find all my Active tasks assigned to optional assignee
         QueryBuilder<Assignable> queryBuilder = qbf.newQueryBuilder( Assignable.class );
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();
         Query<Assignable> assignmentsQuery = queryBuilder.where( and(
               assignee == null ? isNotNull( assignedId ) : eq( assignedId, assignee ),
               eq( ownedId, owner ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ) ) ).
               newQuery( uowf.currentUnitOfWork() );
         return assignmentsQuery.orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );
      }

      public TaskListDTO assignmentsTasks( Assignee assignee )
      {
         return buildTaskList( assignments(assignee ));
      }

      public boolean assignmentsHaveActiveTasks()
      {
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Association<Assignee> assignedId = templateFor( Assignable.Data.class ).assignedTo();
         Association<Owner> ownedId = templateFor( Ownable.Data.class ).owner();
         Query<TaskEntity> assignmentsQuery = queryBuilder.where( and(
               isNotNull( assignedId ),
               eq( ownedId, owner ),
               QueryExpressions.eq( templateFor( Status.Data.class ).status(), States.ACTIVE ) ) ).
               newQuery( uowf.currentUnitOfWork() );

         return assignmentsQuery.count() > 0;
      }

      protected TaskListDTO buildTaskList(
            Iterable<Assignable> assignmentsQuery)
      {
         ValueBuilder<AssignedTaskDTO> builder = vbf.newValueBuilder( AssignedTaskDTO.class );
         AssignedTaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = vbf.newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = vbf.newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (Assignable task : assignmentsQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, (TaskEntity) task );

            list.add( builder.newInstance() );
         }
         return listBuilder.newInstance();
      }

      protected <T extends TaskListDTO> void buildTask( AssignedTaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task )
      {
         prototype.task().set( EntityReference.getEntityReference( task ) );
         if (task.taskType().get() != null)
            prototype.taskType().set( task.taskType().get().getDescription() );
         else
            prototype.taskType().set( null );
         prototype.creationDate().set( task.createdOn().get() );
         prototype.description().set( task.description().get() );
         prototype.status().set( task.status().get() );

         prototype.assignedTo().set( ((Describable)task.assignedTo().get()).getDescription() );

         ValueBuilder<ListValue> labelListBuilder = vbf.newValueBuilder( ListValue.class );
         List<ListItemValue> labelList = labelListBuilder.prototype().items().get();
         for (Label label : task.labels())
         {
            labelPrototype.entity().set( EntityReference.getEntityReference( label ) );
            labelPrototype.description().set( label.getDescription() );
            labelList.add( labelBuilder.newInstance() );
         }
         prototype.labels().set( labelListBuilder.newInstance() );
      }
   }
}