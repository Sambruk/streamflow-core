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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.domain.contact.ContactValue;

/**
 * JAVADOC
 */
@Mixins(Assignments.AssignmentsMixin.class)
public interface Assignments
{
    Task createAssignedTask(Assignee assignee);

    void completeAssignedTask(Task task);

    void dropAssignedTask(Task task);

    void delegateAssignedTaskTo(Task task, Delegatee delegatee);

    void forwardAssignedTask(Task task, Inbox receiverInbox);

    void markAssignedTaskAsRead(Task task);

    void markAssignedTaskAsUnread(Task task);

    void deleteAssignedTask( Task task );

    interface AssignmentsState
    {
        Task assignedTaskCreated(DomainEvent event, String id);
        void assignedTaskMarkedAsRead(DomainEvent event, Task task);
        void assignedTaskMarkedAsUnread(DomainEvent event, Task task);
        void deletedAssignedTask(DomainEvent event, Task task);
        ManyAssociation<Task> unreadAssignedTasks();
    }



    abstract class AssignmentsMixin
            implements Assignments, AssignmentsState
    {
        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Owner owner;

        @This
        WaitingFor waitingFor;

        @Service
        IdentityGenerator idGenerator;

        public Task createAssignedTask(Assignee assignee)
        {
            TaskEntity taskEntity = (TaskEntity) assignedTaskCreated(DomainEvent.CREATE, idGenerator.generate(TaskEntity.class));
            taskEntity.changeOwner(owner);
            taskEntity.addContact(vbf.newValue( ContactValue.class));
            taskEntity.assignTo( assignee );

            return taskEntity;
        }

        public Task assignedTaskCreated(DomainEvent event, String id)
        {
            EntityBuilder<TaskEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(TaskEntity.class, id);
            builder.instance().createdOn().set( event.on().get() );
            return builder.newInstance();
        }

        public void completeAssignedTask(Task task)
        {
            task.complete();
            markAssignedTaskAsRead(task);
        }

        public void dropAssignedTask(Task task)
        {
            task.drop();
            markAssignedTaskAsRead(task);
        }

        public void delegateAssignedTaskTo(Task task, Delegatee delegatee)
        {
            Assignable.AssignableState assignable = (Assignable.AssignableState) task;
            Delegator delegator = (Delegator) assignable.assignedTo().get();
            task.unassign();
            task.delegateTo(delegatee, delegator, waitingFor);
        }

        public void forwardAssignedTask(Task task, Inbox receiverInbox)
        {
            receiverInbox.receiveTask(task);
        }

        public void markAssignedTaskAsRead(Task task)
        {
            if (!unreadAssignedTasks().contains(task))
            {
                return;
            }
            assignedTaskMarkedAsRead(DomainEvent.CREATE, task);
        }

        public void markAssignedTaskAsUnread(Task task)
        {
            if (unreadAssignedTasks().contains(task))
            {
                return;
            }
            assignedTaskMarkedAsUnread(DomainEvent.CREATE, task);
        }

        public void deleteAssignedTask( Task task )
        {
            markAssignedTaskAsRead( task );
            deletedAssignedTask( DomainEvent.CREATE, task );
        }

        public void deletedAssignedTask( DomainEvent event, Task task )
        {
            uowf.currentUnitOfWork().remove( task );
        }

        public void assignedTaskMarkedAsRead(DomainEvent event, Task task)
        {
            unreadAssignedTasks().remove(task);
        }

        public void assignedTaskMarkedAsUnread(DomainEvent event, Task task)
        {
            unreadAssignedTasks().add(task);
        }
    }
}
