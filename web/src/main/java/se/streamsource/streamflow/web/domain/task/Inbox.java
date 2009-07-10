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
@Mixins(Inbox.InboxMixin.class)
public interface Inbox
{
    Task newTask(String description);

    void receiveTask(Task task);

    void completeTask(Task task);

    void assignTo(Task task, Assignee assignee);

    void delegate(Task task, Delegatee delegatee);

    void markAsRead(Task task);

    void markAsUnread(Task task);


    class InboxMixin
            implements Inbox
    {
        @This
        Owner owner;

        @This
        Assignee assignee;

        @Structure
        UnitOfWorkFactory uowf;

        public Task newTask(String description)
        {
            EntityBuilder<TaskEntity> taskBuilder = uowf.currentUnitOfWork().newEntityBuilder(TaskEntity.class);
            TaskEntity state = taskBuilder.prototype();
            state.description().set(description);
            state.ownedBy(owner);

            return taskBuilder.newInstance();
        }

        public void receiveTask(Task task)
        {
            task.ownedBy(owner);
        }

        public void completeTask(Task task)
        {
            // Ensure that task is assigned to user first
            task.assignTo(assignee);

            // Complete task
            task.complete();
        }

        public void assignTo(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
        }

        public void delegate(Task task, Delegatee delegatee)
        {
            task.delegateTo(delegatee);
        }

        public void markAsRead(Task task)
        {
            task.markAsRead();
        }

        public void markAsUnread(Task task)
        {
            task.markAsUnread();
        }
    }

}
