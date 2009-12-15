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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.util.Date;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel implements Refreshable, EventListener,
      EventHandler

{
   @Structure
   ValueBuilderFactory vbf;

   EventHandlerFilter eventFilter;

   private CommandQueryClient client;

   TaskGeneralDTO general;

   @Uses
   PossibleTaskTypesModel taskTypesModel;

   @Uses
   TaskLabelsModel taskLabelsModel;

   @Uses
   TaskLabelSelectionModel selectionModel;

   public TaskGeneralModel( @Uses CommandQueryClient client )
   {
      this.client = client;
      eventFilter = new EventHandlerFilter( client.getReference().getParentRef().getLastSegment(), this, "addedLabel",
            "removedLabel", "changedOwner", "changedTaskType" );
   }

   public TaskGeneralDTO getGeneral()
   {
      if (general == null)
         refresh();

      return general;
   }

   public void changeDescription( String newDescription )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( newDescription );
         client.putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(
               TaskResources.could_not_change_description, e );
      }
   }

   public void changeNote( String newNote )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( newNote );
         client.putCommand( "changenote", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_change_note, e );
      }
   }

   public void changeDueOn( Date newDueOn )
   {
      try
      {
         ValueBuilder<DateDTO> builder = vbf.newValueBuilder( DateDTO.class );
         builder.prototype().date().set( newDueOn );
         client.putCommand( "changedueon", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_change_due_on,
               e );
      }
   }

   public void changeTaskType( EntityReference taskType )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( taskType );
         client.postCommand( "changetasktype", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_remove_label,
               e );
      }
   }

   public PossibleTaskTypesModel taskTypesModel()
   {
      return taskTypesModel;
   }

   public TaskLabelsModel labelsModel()
   {
      return taskLabelsModel;
   }


   public TaskLabelSelectionModel selectionModel()
   {
      return selectionModel;
   }

   public void refresh()
   {
      try
      {
         general = (TaskGeneralDTO) client.query( "general", TaskGeneralDTO.class )
               .buildWith().prototype();

         taskLabelsModel.setLabels( general.labels().get() );

         taskTypesModel.refresh();

         selectionModel.refresh();

      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.handleEvent( event );

      taskLabelsModel.notifyEvent( event );
      selectionModel.notifyEvent( event );
   }

   public boolean handleEvent( DomainEvent event )
   {
      refresh();
      return true;
   }

}