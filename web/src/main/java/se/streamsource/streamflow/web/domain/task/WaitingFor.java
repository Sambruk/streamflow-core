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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(WaitingFor.WaitingForMixin.class)
public interface WaitingFor
{
    void completeWaitingForTask(Task task, Assignee assignee);

    void completeFinishedTask(Task task);

    void rejectFinishedTask(Task task);

    void dropWaitingForTask(Task task, Assignee assignee);

    void assignWaitingForTask(Task task, Assignee assignee);

    void markWaitingForAsRead(Task task);

    void markWaitingForAsUnread(Task task);

    void rejectTask(Task task);

    void deleteWaitingForTask( Task task );

    interface WaitingForState
    {
        ManyAssociation<Task> unreadWaitingForTasks();
        void waitingForTaskMarkedAsUnread(DomainEvent event, Task task);
        void waitingForTaskMarkedAsRead(DomainEvent event, Task task);
        void deletedWaitingForTask(DomainEvent event, Task task);
    }


    abstract class WaitingForMixin
            implements WaitingFor, WaitingForState
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        Owner owner;

        public void completeWaitingForTask(Task task, Assignee assignee)
        {
            task.changeOwner(owner);
            task.assignTo(assignee);
            task.complete();
        }

        public void completeFinishedTask(Task task)
        {
            task.complete();
        }

        public void rejectFinishedTask(Task task)
        {
            task.activate();
        }

        public void dropWaitingForTask(Task task, Assignee assignee)
        {
            task.changeOwner(owner);
            task.assignTo(assignee);
            task.drop();
        }

        public void assignWaitingForTask( Task task, Assignee assignee )
        {
            task.unassign();
            task.rejectDelegation();
            task.changeOwner(owner);
             task.assignTo( assignee );
        }

        public void markWaitingForAsRead(Task task)
        {
            if (!unreadWaitingForTasks().contains(task))
            {
                return;
            }
            waitingForTaskMarkedAsRead(DomainEvent.CREATE, task);
        }

        public void markWaitingForAsUnread(Task task)
        {
            if (unreadWaitingForTasks().contains(task))
            {
                return;
            }
            waitingForTaskMarkedAsUnread(DomainEvent.CREATE, task);
        }

        public void rejectTask(Task task)
        {
            waitingForTaskMarkedAsUnread(DomainEvent.CREATE, task);
        }

        public void deleteWaitingForTask( Task task )
        {
            markWaitingForAsRead( task );
            deletedWaitingForTask( DomainEvent.CREATE, task );
        }

        public void deletedWaitingForTask( DomainEvent event, Task task )
        {
            uowf.currentUnitOfWork().remove( task );
        }

        public void waitingForTaskMarkedAsUnread(DomainEvent event, Task task)
        {
            unreadWaitingForTasks().add(task);
        }

        public void waitingForTaskMarkedAsRead(DomainEvent event, Task task)
        {
            unreadWaitingForTasks().remove(task);
        }
    }
}
