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
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
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
        Task taskCreated(DomainEvent event, String id);
        void markedAsRead(DomainEvent event, Task task);
        void markedAsUnread(DomainEvent event, Task task);
        ManyAssociation<Task> unreadInboxTasks();
    }

    abstract class InboxMixin
            implements Inbox, InboxState
    {
        @This
        Owner owner;

        @This
        WaitingFor waitingFor;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        IdentityGenerator idGenerator;

        public Task createTask()
        {
            TaskEntity taskEntity = (TaskEntity) taskCreated(DomainEvent.CREATE, idGenerator.generate(TaskEntity.class));
            taskEntity.changeOwner(owner);

            return taskEntity;
        }

        public void receiveTask(Task task)
        {
            task.unassign();
            task.changeOwner(owner);
            markAsUnread(task);
        }

        public void completeTask(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
            task.complete();
        }

        public void dropTask(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
            task.drop();
        }

        public void assignTo(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
        }

        public void delegateTo(Task task, Delegatee delegatee, Delegator delegator)
        {
            task.delegateTo(delegatee, delegator, waitingFor);
        }

        public void markAsRead(Task task)
        {
            if (!unreadInboxTasks().contains(task))
            {
                return;
            }
            markedAsRead(DomainEvent.CREATE, task);
        }

        public void markAsUnread(Task task)
        {
            if (unreadInboxTasks().contains(task))
            {
                return;
            }
            markedAsUnread(DomainEvent.CREATE, task);
        }

        public Task taskCreated(DomainEvent event, String id)
        {
            EntityBuilder<TaskEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(TaskEntity.class, id);
            builder.instance().createdOn().set( event.on().get() );
            return builder.newInstance();
        }

        public void markedAsRead(DomainEvent event, Task task)
        {
            unreadInboxTasks().remove(task);
        }

        public void markedAsUnread(DomainEvent event, Task task)
        {
            unreadInboxTasks().add(task);
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
