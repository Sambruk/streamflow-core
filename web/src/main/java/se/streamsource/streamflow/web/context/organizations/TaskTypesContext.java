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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(TaskTypesContext.Mixin.class)
public interface TaskTypesContext
   extends SubContexts<TaskTypeContext>, IndexContext<LinksValue>, Context
{
   public void createtasktype( StringDTO name );

   abstract class Mixin
      extends ContextMixin
      implements TaskTypesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         TaskTypes.Data taskTypes = context.role(TaskTypes.Data.class);
         return new LinksBuilder(module.valueBuilderFactory()).rel( "tasktype" ).addDescribables( taskTypes.taskTypes()).newLinks();
      }

      public void createtasktype( StringDTO name )
      {
         TaskTypes taskTypes = context.role(TaskTypes.class);
         taskTypes.createTaskType( name.string().get() );
      }

      public TaskTypeContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( TaskType.class, id ));

         return subContext( TaskTypeContext.class );
      }
   }
}
