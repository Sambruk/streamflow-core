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

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Concerns(Inbox.CreateTaskConcern.class)
@Mixins(Inbox.InboxMixin.class)
public interface Inbox
{
    Task createTask();

    void receiveTask(Task task);

    void completeTask(Task task, Assignee assignee);

    void dropTask(Task task, Assignee assignee);

    void assignTo(Task task, Assignee assignee);

    void delegateTo(Task task, Delegatee delegatee, Delegator delegator);

    void markAsRead(Task task);

    void markAsUnread(Task task);

    interface InboxState
    {
        Task taskCreated(DomainEvent event);
    }


    class InboxMixin
            implements Inbox, InboxState
    {
        @This
        Owner owner;

        @This
        Delegations delegations;

        @Structure
        UnitOfWorkFactory uowf;

        public Task createTask()
        {
            TaskEntity taskEntity = (TaskEntity) taskCreated(DomainEvent.CREATE);
            taskEntity.changeOwner(owner);
            taskEntity.markAsRead();

            return taskEntity;
        }

        public void receiveTask(Task task)
        {
            task.unassign();
            task.changeOwner(owner);
            task.markAsUnread();
        }

        public void completeTask(Task task, Assignee assignee)
        {
            task.complete(assignee);
        }

        public void dropTask(Task task, Assignee assignee)
        {
            task.drop(assignee);
        }

        public void assignTo(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
        }

        public void delegateTo(Task task, Delegatee delegatee, Delegator delegator)
        {
            task.delegateTo(delegatee, delegator, delegations);
        }

        public void markAsRead(Task task)
        {
            task.markAsRead();
        }

        public void markAsUnread(Task task)
        {
            task.markAsUnread();
        }

        public Task taskCreated(DomainEvent event)
        {
            return uowf.currentUnitOfWork().newEntity(TaskEntity.class);
        }
    }

    abstract class CreateTaskConcern
            extends ConcernOf<Inbox>
            implements Inbox
    {

        @Structure
        ValueBuilderFactory vbf;


        public Task createTask()
        {

            Task task = next.createTask();

            task.addContact(vbf.newValue(ContactValue.class));
            return task;
        }
    }


}
