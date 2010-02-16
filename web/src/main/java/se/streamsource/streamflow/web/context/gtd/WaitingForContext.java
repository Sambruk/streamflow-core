/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.gtd;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegator;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;

import java.util.List;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(WaitingForContext.Mixin.class)
public interface WaitingForContext
   extends Context
{
   TaskListDTO tasks();

   abstract class Mixin
      extends ContextMixin
      implements WaitingForContext
   {
      public TaskListDTO tasks( )
      {
         WaitingForQueries waitingForQueries = context.role( WaitingForQueries.class);

         QueryBuilder<Delegatable> builder = waitingForQueries.waitingFor( context.role( Delegator.class ));
         Query<Delegatable> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         query = query.orderBy( orderBy( templateFor( Delegatable.Data.class ).delegatedOn() ) );
         return buildTaskList(query, module.valueBuilderFactory());
      }

      public static TaskListDTO buildTaskList(
            Query<Delegatable> inboxQuery, ValueBuilderFactory vbf)
      {
         ValueBuilder<WaitingForTaskDTO> builder = vbf.newValueBuilder( WaitingForTaskDTO.class );
         TaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = vbf.newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = vbf.newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (Delegatable task : inboxQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, (TaskEntity) task, vbf );

            list.add( builder.newInstance() );
         }
         return listBuilder.newInstance();
      }

      protected static <T extends TaskListDTO> void buildTask( TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task, ValueBuilderFactory vbf )
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
            taskDTO.assignedTo().set( ((Describable)assignee).getDescription() );
         else
            taskDTO.assignedTo().set( "" );
         taskDTO.delegatedTo().set( ((Describable)task.delegatedTo().get()).getDescription() );
         taskDTO.delegatedOn().set( task.delegatedOn().get() );
      }
   }
}