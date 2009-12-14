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

package se.streamsource.streamflow.web.resource.users.workspace;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * JAVADOC
 */
public class AbstractTaskListServerResource
      extends CommandQueryServerResource
{

   protected <T extends TaskListDTO, V extends TaskDTO> T buildTaskList(
         Query<TaskEntity> inboxQuery,
         Class<V> taskClass,
         Class<T> taskListClass )
   {
      ValueBuilder<V> builder = vbf.newValueBuilder( taskClass );
      TaskDTO prototype = builder.prototype();
      ValueBuilder<T> listBuilder = vbf.newValueBuilder( taskListClass );
      T t = listBuilder.prototype();
      Property<List<V>> property = t.tasks();
      List<V> list = property.get();
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
      prototype.creationDate().set( task.createdOn().get() );
      prototype.description().set( task.description().get() );
      prototype.status().set( task.status().get() );
      prototype.isRead().set( true );

      addAdditionalValues( prototype, task );

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


   public void createTask( String inboxId )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      Inbox inbox = uow.get( Inbox.class, inboxId );
      inbox.createTask();
   }

   /**
    * Should be overriden by subclasses if there is a need to add more specialized properties.
    *
    * @param prototype
    * @param task
    */
   protected void addAdditionalValues( TaskDTO prototype, TaskEntity task )
   {

   }
}
