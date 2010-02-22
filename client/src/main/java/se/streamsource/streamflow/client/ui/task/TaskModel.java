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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * Model for task details.
 */
public class TaskModel
      implements EventListener
{
   @Uses
   private TaskInfoModel info;

   @Uses
   private TaskActionsModel actions;

   @Uses
   private TaskConversationsModel conversations;

   @Uses
   private TaskGeneralModel general;

   @Uses
   private TaskContactsModel contacts;

   @Uses
   private TaskFormsModel forms;

   @Uses
   CommandQueryClient client;

   public String taskId()
   {
      return client.getReference().getLastSegment();
   }

   public TaskInfoModel info()
   {
      return info;
   }
   
   public TaskConversationsModel conversations()
   {
      return conversations;
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

   public TaskActionsModel actions()
   {
      return actions;
   }

   public void notifyEvent( DomainEvent event )
   {
      info.notifyEvent( event );
      conversations.notifyEvent( event );
      general.notifyEvent( event );
      contacts.notifyEvent( event );
      forms.notifyEvent( event );
   }
}