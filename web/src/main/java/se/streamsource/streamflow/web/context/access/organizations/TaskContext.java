/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.task.ProxyUserTaskDTO;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.task.Task;

/**
 * JAVADOC
 */
@Mixins(TaskContext.Mixin.class)
public interface TaskContext
      extends IndexContext<ProxyUserTaskDTO>, Context
{
   void changedescription( StringValue newDescription );

   void sendtofunction();

   abstract class Mixin
         extends ContextMixin
         implements TaskContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public ProxyUserTaskDTO index()
      {
         Task task = context.role( Task.class );

         ValueBuilder<ProxyUserTaskDTO> builder = vbf.newValueBuilder( ProxyUserTaskDTO.class );
         builder.prototype().description().set( task.getDescription() );
         AccessPoint.Data accessPoint = context.role( AccessPoint.Data.class );

         builder.prototype().project().set( accessPoint.project().get().getDescription() );
         builder.prototype().taskType().set( accessPoint.taskType().get().getDescription() );


         for (Label label : accessPoint.labels().get())
         {
            builder.prototype().labels().get().add( label.getDescription() );
         }
         return builder.newInstance();
      }

      public void changedescription( StringValue newDescription )
      {
         Describable describable = context.role( Describable.class );
         describable.changeDescription( newDescription.string().get() );
      }

      public void sendtofunction()
      {
         TaskEntity task = context.role(TaskEntity.class);
         ProjectEntity project = (ProjectEntity) context.role( AccessPoint.Data.class ).project().get();

         task.unassign();
         task.sendTo( project );
      }
   }
}