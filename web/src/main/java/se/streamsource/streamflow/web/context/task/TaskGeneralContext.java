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
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.task.TaskTypeQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.dci.infrastructure.web.context.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;

/**
 * JAVADOC
 */
@Mixins(TaskGeneralContext.Mixin.class)
public interface TaskGeneralContext
   extends DescribableContext,
      NotableContext,
      IndexContext<TaskGeneralDTO>,
      Context
{
   void changedueon( DateDTO dueOnValue );
   LinksValue possibletasktypes();
   void tasktype( EntityReferenceDTO dto );
   LinksValue possibleforms();

   @SubContext
   LabelableContext labels();

   abstract class Mixin
      extends ContextMixin
      implements TaskGeneralContext
   {
      @Structure
      Module module;

      public TaskGeneralDTO index()
      {
         ValueBuilderFactory vbf = module.valueBuilderFactory();
         ValueBuilder<TaskGeneralDTO> builder = vbf.newValueBuilder( TaskGeneralDTO.class );
         TaskEntity task = context.role( TaskEntity.class );
         builder.prototype().description().set( task.description().get() );

         ValueBuilder<ListValue> labelsBuilder = vbf.newValueBuilder( ListValue.class );
         ValueBuilder<ListItemValue> labelsItemBuilder = vbf.newValueBuilder( ListItemValue.class );
         for (Label label : task.labels())
         {
            labelsItemBuilder.prototype().entity().set( EntityReference.getEntityReference( label ) );
            labelsItemBuilder.prototype().description().set( label.getDescription() );
            labelsBuilder.prototype().items().get().add( labelsItemBuilder.newInstance() );
         }

         TaskType taskType = task.taskType().get();
         if (taskType != null)
         {
            ValueBuilder<ListItemValue> taskTypeBuilder = vbf.newValueBuilder( ListItemValue.class );
            taskTypeBuilder.prototype().description().set( taskType.getDescription() );
            taskTypeBuilder.prototype().entity().set( EntityReference.getEntityReference( taskType ) );
            builder.prototype().taskType().set( taskTypeBuilder.newInstance() );
         }

         builder.prototype().labels().set( labelsBuilder.newInstance() );
         builder.prototype().note().set( task.note().get() );
         builder.prototype().creationDate().set( task.createdOn().get() );
         builder.prototype().taskId().set( task.taskId().get() );
         builder.prototype().dueOn().set( task.dueOn().get() );

         return builder.newInstance();
      }

      public void changedueon( DateDTO dueOnValue )
      {
         DueOn dueOn = context.role(DueOn.class);
         dueOn.dueOn( dueOnValue.date().get() );
      }

      public LinksValue possibletasktypes()
      {
         TaskTypeQueries task = context.role(TaskTypeQueries.class);
         return new LinksBuilder(module.valueBuilderFactory()).command( "tasktype" ).addDescribables( task.taskTypes()).newLinks();
      }

      public void tasktype( EntityReferenceDTO dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         TypedTask task = context.role(TypedTask.class);

         EntityReference entityReference = dto.entity().get();
         if (entityReference != null)
         {
            TaskType taskType = uow.get( TaskType.class, entityReference.identity() );
            task.changeTaskType( taskType );
         } else
            task.changeTaskType( null );
      }

      public LinksValue possibleforms()
      {
         TypedTask.Data typedTask = context.role(TypedTask.Data.class);

         TaskType taskType = typedTask.taskType().get();

         ListValue formsList;
         if (taskType != null)
         {
            Forms.Data forms = (Forms.Data) taskType;
            return new LinksBuilder(module.valueBuilderFactory()).addDescribables( forms.forms() ).newLinks();
         } else
         {
            return new LinksBuilder(module.valueBuilderFactory()).newLinks();
         }
      }

      public LabelableContext labels()
      {
         return subContext( LabelableContext.class );
      }
   }
}
