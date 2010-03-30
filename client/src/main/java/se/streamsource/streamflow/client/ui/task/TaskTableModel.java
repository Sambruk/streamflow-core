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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.task.TaskValue;

import java.util.List;

/**
 * Base class for all models that list tasks
 */
public class TaskTableModel
      implements EventListener, EventVisitor, Refreshable
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected ValueBuilderFactory vbf;

   protected LinksValue tasks;

   protected BasicEventList<TaskValue> eventList = new BasicEventList<TaskValue>();

   private EventVisitorFilter eventFilter;

   public TaskTableModel()
   {
      eventFilter = new EventVisitorFilter( this, "addedLabel", "removedLabel", "changedDescription", "changedTaskType", "changedStatus",
            "sentTo","assignedTo","delegatedTo", "deletedEntity");
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( final DomainEvent event )
   {
      TaskValue updatedTask = getTask( event );

      if (updatedTask != null)
      {
         int idx = eventList.indexOf( updatedTask );
         ValueBuilder<TaskValue> valueBuilder = updatedTask.<TaskValue>buildWith();
         updatedTask = valueBuilder.prototype();

         String eventName = event.name().get();
         if (eventName.equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updatedTask.text().set( newDesc );
               eventList.set( idx, valueBuilder.newInstance() );
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         } else if (eventName.equals( "removedLabel" ))
         {
            String id = EventParameters.getParameter( event, "param1" );
            List<LinkValue> labels = updatedTask.labels().get().links().get();
            for (LinkValue label : labels)
            {
               if (label.id().get().equals( id ))
               {
                  labels.remove( label );
                  break;
               }
            }
            eventList.set( idx, valueBuilder.newInstance() );
         } else if ("addedLabel,changedTaskType,sentTo,assignedTo,delegatedTo,deletedEntity".indexOf(eventName) != -1)
         {
            refresh();
         } else if (eventName.equals("changedStatus"))
         {
            States newStatus = States.valueOf( EventParameters.getParameter( event, "param1" ));
            updatedTask.status().set( newStatus );
            eventList.set( idx, valueBuilder.newInstance() );            
         }
      }
      return true;
   }

   public EventList<TaskValue> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         final LinksValue newRoot = client.query( "tasks", LinksValue.class );
         boolean same = newRoot.equals( tasks );
         if (!same)
         {
               EventListSynch.synchronize( newRoot.links().get(), eventList );
               tasks = newRoot;
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   private TaskValue getTask( DomainEvent event )
   {
      if (tasks == null)
         return null;

      for (int i = 0; i < eventList.size(); i++)
      {
         TaskValue taskValue = eventList.get( i );
         if (taskValue.id().get().equals( event.entity().get() ))
         {
            return taskValue;
         }
      }

      return null;
   }
}