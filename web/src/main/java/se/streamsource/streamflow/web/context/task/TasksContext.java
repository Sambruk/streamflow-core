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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.user.SearchTaskQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(TasksContext.Mixin.class)
public interface TasksContext
   extends SubContexts<TaskContext>, Context
{
   TaskListDTO search( StringDTO query);

   abstract class Mixin
      extends ContextMixin
      implements TasksContext
   {
      @Structure
      Module module;

      public TaskListDTO search( StringDTO query )
      {
         SearchTaskQueries taskQueries = context.role( SearchTaskQueries.class );
         String name = context.role( UserAuthentication.Data.class ).userName().get();
         return buildTaskList( taskQueries.search( query, name ), SearchTaskDTO.class);
      }

      public TaskContext context( String id )
      {
         TaskEntity task = module.unitOfWorkFactory().currentUnitOfWork().get( TaskEntity.class, id );
         context.playRoles( task, TaskEntity.class );

         return subContext( TaskContext.class);
      }

      protected <V extends TaskDTO> TaskListDTO buildTaskList(
            Query<Task> inboxQuery,
            Class<V> taskClass)
      {
         ValueBuilder<V> builder = module.valueBuilderFactory().newValueBuilder( taskClass );
         TaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = module.valueBuilderFactory().newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = module.valueBuilderFactory().newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (Task task : inboxQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, (TaskEntity) task );

            list.add( builder.newInstance() );
         }
         return listBuilder.newInstance();
      }

      protected void buildTask( TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task )
      {
         prototype.task().set( EntityReference.getEntityReference( task ) );

         if (task.taskType().get() != null)
            prototype.taskType().set( task.taskType().get().getDescription() );
         else
            prototype.taskType().set( null );

         prototype.creationDate().set( task.createdOn().get() );
         prototype.description().set( task.description().get() );
         prototype.status().set( task.status().get() );

         addAdditionalValues( prototype, task );

         ValueBuilder<ListValue> labelListBuilder = module.valueBuilderFactory().newValueBuilder( ListValue.class );
         List<ListItemValue> labelList = labelListBuilder.prototype().items().get();
         for (Label label : task.labels())
         {
            labelPrototype.entity().set( EntityReference.getEntityReference( label ) );
            labelPrototype.description().set( label.getDescription() );
            labelList.add( labelBuilder.newInstance() );
         }
         prototype.labels().set( labelListBuilder.newInstance() );

      }

      protected void addAdditionalValues( TaskDTO prototype, TaskEntity task )
      {
         if (task.assignedTo().get() != null)
         {
            ((SearchTaskDTO) prototype).assignedTo().set( ((Describable) task.assignedTo().get()).getDescription() );
         } else
         {
            ((SearchTaskDTO) prototype).assignedTo().set( null );
         }
         ((SearchTaskDTO) prototype).project().set( ((Describable) task.owner().get()).getDescription() );

      }
   }
}
