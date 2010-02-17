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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.user.SearchTaskQueries;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(TasksContext.Mixin.class)
public interface TasksContext
   extends SubContexts<TaskContext>, Context
{
   LinksValue search( StringDTO query);

   abstract class Mixin
      extends ContextMixin
      implements TasksContext
   {
      public static LinksValue buildTaskList(Query<Task> query, Module module)
      {
         LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());
         for (Task task : query)
         {
            linksBuilder.addLink( TaskContext.Mixin.taskDTO( (TaskEntity) task, module));
         }
         return linksBuilder.newLinks();
      }

      @Structure
      Module module;

      public LinksValue search( StringDTO query )
      {
         SearchTaskQueries taskQueries = context.role( SearchTaskQueries.class );
         String name = context.role( UserAuthentication.Data.class ).userName().get();
         return buildTaskList( taskQueries.search( query, name ), module);
      }

      public TaskContext context( String id )
      {
         TaskEntity task = module.unitOfWorkFactory().currentUnitOfWork().get( TaskEntity.class, id );
         context.playRoles( task, TaskEntity.class );

         return subContext( TaskContext.class);
      }
   }
}
