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

package se.streamsource.streamflow.web.domain.structure.tasktype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
@Mixins(TypedTask.Mixin.class)
public interface TypedTask
{
   void changeTaskType( @Optional TaskType newTaskType );

   interface Data
   {
      @Optional
      Association<TaskType> taskType();

      void changedTaskType( DomainEvent event, @Optional TaskType taskType );
   }

   abstract class Mixin
         implements Data
   {
      @This
      Labelable labelable;

      public void changedTaskType( DomainEvent event, @Optional TaskType taskType )
      {
         TaskType currentTaskType = taskType().get();
         if ((currentTaskType == null && taskType != null) || !currentTaskType.equals( taskType ))
         {
            labelable.retainLabels( (SelectedLabels) currentTaskType, (SelectedLabels) taskType );

            taskType().set( taskType );
         }
      }
   }
}
