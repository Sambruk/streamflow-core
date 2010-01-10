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

package se.streamsource.streamflow.web.domain.structure.tasktype;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(TaskTypes.Mixin.class)
public interface TaskTypes
{
   // Commands
   TaskType createTaskType( String name );

   boolean removeTaskType( TaskType taskType );

   interface Data
   {
      @Aggregated
      ManyAssociation<TaskType> taskTypes();

      TaskType createdTaskType( DomainEvent event, String id );

      void removedTaskType( DomainEvent event, TaskType taskType );
   }

   abstract class Mixin
         implements TaskTypes, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      public TaskType createTaskType( String name )
      {
         TaskType taskType = createdTaskType( DomainEvent.CREATE, idGen.generate( Identity.class ) );
         taskType.changeDescription( name );

         return taskType;
      }

      public TaskType createdTaskType( DomainEvent event, String id )
      {
         TaskType taskType = uowf.currentUnitOfWork().newEntity( TaskType.class, id );
         taskTypes().add( taskType );

         return taskType;
      }
   }
}