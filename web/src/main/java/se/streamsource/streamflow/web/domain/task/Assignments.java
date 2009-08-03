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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * JAVADOC
 */
@Mixins(Assignments.AssignmentsMixin.class)
public interface Assignments
{
    Task newAssignedTask(Assignee assignee);

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

        public Task newAssignedTask(Assignee assignee)
        {
            EntityBuilder<TaskEntity> taskBuilder = uowf.currentUnitOfWork().newEntityBuilder(TaskEntity.class);
            TaskEntity state = taskBuilder.prototype();
            state.ownedBy(owner);
            Task task = taskBuilder.newInstance();
            task.assignTo(assignee);
            return task;
        }

        public void completeAssignedTask(Task task, Assignee assignee)
        {
            task.completedBy(assignee);
        }

        public void dropAssignedTask(Task task, Assignee assignee)
        {
            task.droppedBy(assignee);
        }

        public void delegateAssignedTaskTo(Task task, Delegatee delegatee, Delegator delegator)
        {
            task.assignTo(null);
            task.delegateTo(delegatee,  delegator);
        }

        public void forwardAssignedTask(Task task, Inbox receiverInbox)
        {
            task.assignTo(null);
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
