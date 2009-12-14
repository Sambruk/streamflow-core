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
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.task.TaskClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * Model for task details.
 */
public class TaskModel
      implements EventListener
{
   @Uses
   private TaskClientResource resource;

   @Uses
   private TaskCommentsModel comments;

   @Uses
   private TaskGeneralModel general;

   @Uses
   private TaskContactsModel contacts;

   @Uses
   private TaskFormsModel forms;

   public TaskClientResource resource()
   {
      return resource;
   }

   public TaskCommentsModel comments()
   {
      return comments;
   }

   public TaskGeneralModel general()
   {
      return general;
   }

   public TaskContactsModel contacts()
   {
      return contacts;
   }

   public TaskFormsModel forms()
   {
      return forms;
   }

   public void notifyEvent( DomainEvent event )
   {
      comments.notifyEvent( event );
      general.notifyEvent( event );
      contacts.notifyEvent( event );
      forms.notifyEvent( event );
   }

   public EventList<ListItemValue> getPossibleProjects()
   {
      try
      {
         BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

         ListValue listValue = resource.possibleProjects();
         list.addAll( listValue.items().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }
}