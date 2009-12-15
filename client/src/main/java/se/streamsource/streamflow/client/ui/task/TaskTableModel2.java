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
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.SwingUtilities;

/**
 * Base class for all models that list tasks
 */
public class TaskTableModel2
      implements EventListener, EventHandler, Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   protected TaskListDTO tasks;

   private EventHandlerFilter eventFilter;

   private BasicEventList<TaskDTO> eventList = new BasicEventList<TaskDTO>();

   public TaskTableModel2()
   {
      eventFilter = new EventHandlerFilter( this, "addedLabel", "removedLabel", "changedDescription" );
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.handleEvent( event );
   }

   public boolean handleEvent( final DomainEvent event )
   {
/*
      final int idx = getTaskIndex( event );

      if (idx != -1)
      {
         final TaskDTO updatedTask = getTask( idx );
         if (event.name().get().equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updatedTask.description().set( newDesc );
               fireTableCellUpdated( idx, 1 );
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         } else if (event.name().get().equals( "removedLabel" ))
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
         } else if (event.name().get().equals( "addedLabel" ))
         {
            SwingUtilities.invokeLater(
                  new Runnable()
                  {
                     public void run()
                     {
                        List<ListItemValue> labels = tasksModel.models
                              .get( updatedTask.task().get().identity() )
                              .general().getGeneral().labels().get().items().get();
                        for (ListItemValue label : labels)
                        {
                           if (label.entity().get().identity()
                                 .equals( EventParameters.getParameter( event, "param1" ) ))
                           {
                              List<ListItemValue> newLabels = updatedTask.labels().get().items().get();
                              newLabels.add( label );
                           }
                        }
                     }
                  } );
         }
      }
*/
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
                  eventList.clear();
                  eventList.addAll( newRoot.tasks().get() );
               }
            });
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   private int getTaskIndex( DomainEvent event )
   {
      if (tasks == null)
         return -1;

      TaskDTO updatedTask = null;
      for (int i = 0; i < tasks.tasks().get().size(); i++)
      {
         TaskDTO taskDTO = tasks.tasks().get().get( i );
         if (taskDTO.task().get().identity().equals( event.entity().get() ))
         {
            return i;
         }
      }

      return -1;
   }
}