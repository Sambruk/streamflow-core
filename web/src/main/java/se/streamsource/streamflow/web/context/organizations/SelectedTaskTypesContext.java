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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(SelectedTaskTypesContext.Mixin.class)
public interface SelectedTaskTypesContext
   extends SubContexts<SelectedTaskTypeContext>, IndexContext<LinksValue>, Context
{
   public LinksValue possibletasktypes();

   public void addtasktype( EntityReferenceDTO taskTypeDTO );

   abstract class Mixin
      extends ContextMixin
      implements SelectedTaskTypesContext
   {
      public LinksValue index()
      {
         SelectedTaskTypes.Data taskTypes = context.role(SelectedTaskTypes.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel("tasktype").addDescribables( taskTypes.selectedTaskTypes() ).newLinks();
      }

      public LinksValue possibletasktypes()
      {
         SelectedTaskTypes.Data selectedLabels = context.role(SelectedTaskTypes.Data.class);
         TaskTypes.Data taskTypes = context.role(TaskTypes.Data.class);
         return new LinksBuilder(module.valueBuilderFactory()).command( "addtasktype" ).addDescribables(selectedLabels.possibleTaskTypes( taskTypes.taskTypes() )).newLinks();
      }

      public void addtasktype( EntityReferenceDTO taskTypeDTO )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         SelectedTaskTypes taskTypes = context.role(SelectedTaskTypes.class);
         TaskType taskType = uow.get( TaskType.class, taskTypeDTO.entity().get().identity() );

         taskTypes.addSelectedTaskType( taskType );
      }

      public SelectedTaskTypeContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( TaskType.class, id ));
         return subContext( SelectedTaskTypeContext.class );
      }
   }
}
