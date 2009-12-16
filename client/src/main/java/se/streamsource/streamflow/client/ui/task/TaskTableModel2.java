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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventParameters;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Base class for all models that list tasks
 */
public class TaskTableModel2
      implements EventListener, EventHandler, Refreshable
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected ValueBuilderFactory vbf;

   protected TaskListDTO tasks;

   protected BasicEventList<TaskDTO> eventList = new BasicEventList<TaskDTO>();

   private EventHandlerFilter eventFilter;

   public TaskTableModel2()
   {
      eventFilter = new EventHandlerFilter( this, "addedLabel", "removedLabel", "changedDescription", "changedTaskType", "changedStatus" );
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.handleEvent( event );
   }

   public boolean handleEvent( final DomainEvent event )
   {
      TaskDTO updatedTask = getTask( event );

      if (updatedTask != null)
      {
         int idx = eventList.indexOf( updatedTask );
         ValueBuilder<TaskDTO> valueBuilder = updatedTask.<TaskDTO>buildWith();
         updatedTask = valueBuilder.prototype();

         String eventName = event.name().get();
         if (eventName.equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updatedTask.description().set( newDesc );
               eventList.set( idx, valueBuilder.newInstance() );
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         } else if (eventName.equals( "removedLabel" ))
         {
            String id = EventParameters.getParameter( event, "param1" );
            List<ListItemValue> labels = updatedTask.labels().get().items().get();
            for (ListItemValue label : labels)
            {
               if (label.entity().get().identity().equals( id ))
               {
                  labels.remove( label );
                  break;
               }
            }
            eventList.set( idx, valueBuilder.newInstance() );
         } else if (eventName.equals( "addedLabel" ) || eventName.equals("changedTaskType"))
         {
            refresh();
         } else if (eventName.equals("changedStatus"))
         {
            TaskStates newStatus = TaskStates.valueOf( EventParameters.getParameter( event, "param1" ));
            updatedTask.status().set( newStatus );
            eventList.set( idx, valueBuilder.newInstance() );            
         }
      }
      return true;
   }

   public EventList<TaskDTO> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         final TaskListDTO newRoot = client.query( "tasks", vbf.newValue( TasksQuery.class ), TaskListDTO.class );
         boolean same = newRoot.equals( tasks );
         if (!same)
         {
            SwingUtilities.invokeLater( new Runnable()
            {
               public void run()
               {
                  if (newRoot.tasks().get().size() == eventList.size())
                  {
                     int idx = 0;
                     for (TaskDTO taskDTO : newRoot.tasks().get())
                     {
                        eventList.set( idx++, taskDTO );
                     }
                  } else
                  {
                     eventList.clear();
                     eventList.addAll( newRoot.tasks().get() );
                  }
                  tasks = newRoot;
               }
            });
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   private TaskDTO getTask( DomainEvent event )
   {
      if (tasks == null)
         return null;

      for (int i = 0; i < eventList.size(); i++)
      {
         TaskDTO taskDTO = eventList.get( i );
         if (taskDTO.task().get().identity().equals( event.entity().get() ))
         {
            return taskDTO;
         }
      }

      return null;
   }
}