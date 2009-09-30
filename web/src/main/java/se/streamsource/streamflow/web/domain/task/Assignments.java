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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Assignments.AssignmentsMixin.class)
public interface Assignments
{
    Task createAssignedTask(Assignee assignee);

    void completeAssignedTask(Task task, Assignee assignee);

    void dropAssignedTask(Task task, Assignee assignee);

    void delegateAssignedTaskTo(Task task, Delegatee delegatee, Delegator delegator);

    void forwardAssignedTask(Task task, Inbox receiverInbox);

    void markAssignedTaskAsRead(Task task);

    void markAssignedTaskAsUnread(Task task);

    class AssignmentsMixin
            implements Assignments
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        Owner owner;

        @This
        Delegations delegations;

        @This
        Inbox.InboxState inbox;

        public Task createAssignedTask(Assignee assignee)
        {
            Task task = inbox.taskCreated(DomainEvent.CREATE);
            task.changeOwner(owner);
            task.assignTo(assignee);
            return task;
        }

        public void completeAssignedTask(Task task, Assignee assignee)
        {
            task.complete(assignee);
        }

        public void dropAssignedTask(Task task, Assignee assignee)
        {
            task.drop(assignee);
        }

        public void delegateAssignedTaskTo(Task task, Delegatee delegatee, Delegator delegator)
        {
            task.unassign();
            task.delegateTo(delegatee, delegator, delegations);
        }

        public void forwardAssignedTask(Task task, Inbox receiverInbox)
        {
            receiverInbox.receiveTask(task);
        }

        public void markAssignedTaskAsRead(Task task)
        {
            task.markAsRead();
        }

        public void markAssignedTaskAsUnread(Task task)
        {
            task.markAsUnread();
        }
    }
}
