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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import static se.streamsource.streamflow.domain.task.TaskStates.*;

/**
 * Delegations of tasks
 */
@Mixins(Delegations.Mixin.class)
public interface Delegations
{
   void accept( @HasStatus(ACTIVE) Task task, Assignee assignee );

   void reject( @HasStatus(ACTIVE) Task task );

   void finishDelegatedTask( @HasStatus(ACTIVE) Task task, Assignee assignee );

   void markDelegatedTaskAsRead( Task task );

   void markDelegatedTaskAsUnread( Task task );

   interface Data
   {
      void markedDelegatedTaskAsRead( DomainEvent event, Task task );

      void markedDelegatedTaskAsUnread( DomainEvent event, Task task );

      ManyAssociation<Task> unreadDelegatedTasks();
   }

   abstract class Mixin
         implements Delegations, Data
   {
      @This
      Owner owner;

      public void accept( Task task, Assignee assignee )
      {
         task.assignTo( assignee );
         task.changeOwner( owner );
      }

      public void reject( Task task )
      {
         task.rejectDelegation();
      }

      public void finishDelegatedTask( Task task, Assignee assignee )
      {
         accept( task, assignee );
         task.done();
      }

      public void markDelegatedTaskAsRead( Task task )
      {
         if (!unreadDelegatedTasks().contains( task ))
         {
            return;
         }
         markedDelegatedTaskAsRead( DomainEvent.CREATE, task );
      }

      public void markDelegatedTaskAsUnread( Task task )
      {
         if (unreadDelegatedTasks().contains( task ))
         {
            return;
         }
         markedDelegatedTaskAsUnread( DomainEvent.CREATE, task );
      }

      public void markedDelegatedTaskAsRead( DomainEvent event, Task task )
      {
         unreadDelegatedTasks().remove( task );
      }

      public void markedDelegatedTaskAsUnread( DomainEvent event, Task task )
      {
         unreadDelegatedTasks().add( task );
      }
   }
}
