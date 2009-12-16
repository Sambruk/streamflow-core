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
package se.streamsource.streamflow.web.domain.task;

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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.web.domain.label.Label;

import java.util.List;

@Mixins(WaitingForQueries.Mixin.class)
public interface WaitingForQueries
{
   TaskListDTO waitingForTasks( @Optional Delegator delegator );

   boolean hasActiveOrDoneAndUnreadTasks();

   class Mixin
         implements WaitingForQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      WaitingFor waitingFor;

      @This
      WaitingFor.Data waitingForState;

      public TaskListDTO waitingForTasks( Delegator delegator )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         // Find all Active delegated tasks owned by this Entity and, optionally, delegated by "delegator"
         // or delegated tasks that are marked as done
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Association<Delegator> delegatedBy = templateFor( Delegatable.Data.class ).delegatedBy();
         Association<WaitingFor> waitingFor = templateFor( Delegatable.Data.class ).delegatedFrom();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         Query<TaskEntity> waitingForQuery = queryBuilder.where( and(
               eq( waitingFor, this.waitingFor ),
               delegator != null ? eq( delegatedBy, delegator ) : QueryExpressions.isNotNull( delegatedBy ),
               isNotNull( delegatee ),
               or(
                     eq( templateFor( TaskStatus.Data.class ).status(), TaskStates.ACTIVE ),
                     eq( templateFor( TaskStatus.Data.class ).status(), TaskStates.DONE ) ) ) ).
               newQuery( uow );
         waitingForQuery.orderBy( orderBy( templateFor( Delegatable.Data.class ).delegatedOn() ) );

         return buildTaskList( waitingForQuery, WaitingForTaskDTO.class);
      }

      public boolean hasActiveOrDoneAndUnreadTasks()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         // Find all Active delegated tasks owned by this Entity
         // or Completed delegated tasks that are marked as unread
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Association<Delegator> delegatedBy = templateFor( Delegatable.Data.class ).delegatedBy();
         Association<WaitingFor> waitingFor = templateFor( Delegatable.Data.class ).delegatedFrom();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         Query<TaskEntity> waitingForQuery = queryBuilder.where( and(
               eq( waitingFor, this.waitingFor ),
               isNotNull( delegatee ),
               or(
                     eq( templateFor( TaskStatus.Data.class ).status(), TaskStates.ACTIVE ),
                     eq( templateFor( TaskStatus.Data.class ).status(), TaskStates.DONE ) ) ) ).
               newQuery( uow );

         return waitingForQuery.count() > 0;
      }

      protected TaskListDTO buildTaskList(
            Query<TaskEntity> inboxQuery,
            Class taskClass)
      {
         ValueBuilder<TaskDTO> builder = vbf.newValueBuilder( taskClass );
         TaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = vbf.newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = vbf.newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (TaskEntity task : inboxQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, task );

            list.add( builder.newInstance() );
         }
         return listBuilder.newInstance();
      }

      protected <T extends TaskListDTO> void buildTask( TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task )
      {
         prototype.task().set( EntityReference.getEntityReference( task ) );
         if (task.taskType().get() != null)
            prototype.taskType().set( task.taskType().get().getDescription() );
         else
            prototype.taskType().set( null );
         prototype.creationDate().set( task.createdOn().get() );
         prototype.description().set( task.description().get() );
         prototype.status().set( task.status().get() );

         ValueBuilder<ListValue> labelListBuilder = vbf.newValueBuilder( ListValue.class );
         List<ListItemValue> labelList = labelListBuilder.prototype().items().get();
         for (Label label : task.labels())
         {
            labelPrototype.entity().set( EntityReference.getEntityReference( label ) );
            labelPrototype.description().set( label.getDescription() );
            labelList.add( labelBuilder.newInstance() );
         }
         prototype.labels().set( labelListBuilder.newInstance() );

         WaitingForTaskDTO taskDTO = (WaitingForTaskDTO) prototype;
         Assignee assignee = task.assignedTo().get();
         if (assignee != null)
            taskDTO.assignedTo().set( assignee.getDescription() );
         else
            taskDTO.assignedTo().set( "" );
         taskDTO.delegatedTo().set( task.delegatedTo().get().getDescription() );
         taskDTO.delegatedOn().set( task.delegatedOn().get() );
      }
   }
}