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

package se.streamsource.streamflow.web.resource.task.general;

import static org.qi4j.api.entity.EntityReference.getEntityReference;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.entity.form.FormsQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.task.TaskLabelsQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskTypeQueries;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /task/{task}/general
 */
public class TaskGeneralServerResource
      extends CommandQueryServerResource
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   public TaskGeneralServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   public TaskGeneralDTO general()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      ValueBuilder<TaskGeneralDTO> builder = vbf.newValueBuilder( TaskGeneralDTO.class );
      TaskEntity task = uow.get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
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

   public void changedescription( StringDTO stringValue )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Describable describable = uowf.currentUnitOfWork().get( Describable.class, taskId );
      describable.changeDescription( stringValue.string().get() );
   }

   public void changenote( StringDTO noteValue )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Notable notable = uowf.currentUnitOfWork().get( Notable.class, taskId );
      notable.changeNote( noteValue.string().get() );
   }

   public void changedueon( DateDTO dueOnValue )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      DueOn dueOn = uowf.currentUnitOfWork().get( DueOn.class, taskId );
      dueOn.dueOn( dueOnValue.date().get() );
   }

   public void removelabel( EntityReferenceDTO reference )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );

      TaskEntity task = uow.get( TaskEntity.class, taskId );
      Label label = uow.get( Label.class, reference.entity().get().identity() );

      task.removeLabel( label );
   }

   public ListValue possibletasktypes()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TaskTypeQueries task = uow.get( TaskTypeQueries.class, id );

      return task.taskTypes();
   }

   public void tasktype( EntityReferenceDTO dto )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TypedTask task = uow.get( TypedTask.class, id );

      EntityReference entityReference = dto.entity().get();
      if (entityReference != null)
      {
         TaskType taskType = uow.get( TaskType.class, entityReference.identity() );
         task.changeTaskType( taskType );
      } else
         task.changeTaskType( null );
   }

   public ListValue possiblelabels()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TaskLabelsQueries labels = uow.get( TaskLabelsQueries.class, id );

      return labels.possibleLabels();
   }

   public void label( EntityReferenceDTO reference )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );

      Labelable task = uow.get( Labelable.class, taskId );
      Label label = uow.get( Label.class, reference.entity().get().identity() );

      task.addLabel( label );
   }

   public ListValue possibleforms()
   {
      String taskId = getRequest().getAttributes().get( "task" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();

      TypedTask.Data typedTask = uow.get( TypedTask.Data.class, taskId );

      TaskType taskType = typedTask.taskType().get();

      ListValue formsList;
      if (taskType != null)
      {
         FormsQueries forms = uow.get( FormsQueries.class, getEntityReference( taskType ).identity() );
         formsList = forms.applicableFormDefinitionList();
      } else
      {
         formsList = vbf.newValue( ListValue.class );
      }
      return formsList;
   }
}
