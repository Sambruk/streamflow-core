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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;

import java.util.List;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(InboxContext.Mixin.class)
public interface InboxContext
   extends Context
{
   TaskListDTO tasks();

   void createtask();

   abstract class Mixin
      extends ContextMixin
      implements InboxContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public TaskListDTO tasks( )
      {
         InboxQueries inbox = context.role( InboxQueries.class);

         QueryBuilder<Task> builder = inbox.inbox();
         Query<Task> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );
         
         return buildTaskList(query, InboxTaskDTO.class);
      }

      public void createtask()
      {
         Inbox inbox = context.role( Inbox.class );
         inbox.createTask();
      }

      protected <V extends TaskDTO> TaskListDTO buildTaskList(
            Query<Task> inboxQuery,
            Class<V> taskClass)
      {
         ValueBuilder<V> builder = vbf.newValueBuilder( taskClass );
         TaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = vbf.newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = vbf.newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (Task task : inboxQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, (TaskEntity) task );

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
      }
   }
}
