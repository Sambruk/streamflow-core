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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.Event;

/**
 * Status for a task. Possible transitions are:
 * Active -> Completed, Dropped
 * Completed -> Archived
 * Dropped -> Archived
 */
@Mixins(TaskStatus.TaskStatusMixin.class)
public interface TaskStatus
{
    void complete(Assignee assignee);

    void drop(Assignee assignee);

    interface TaskStatusState
    {
        @UseDefaults
        Property<TaskStates> status();

        @Event
        void completed(DomainEvent event);

        @Event
        void dropped(DomainEvent event);
    }

    abstract class TaskStatusMixin
            implements TaskStatus, TaskStatusState
    {
        public void complete(Assignee assignee)
        {
            if (status().get().equals(TaskStates.ACTIVE))
            {
                completed(DomainEvent.CREATE);
            }
        }

        public void drop(Assignee assignee)
        {
            if (status().get().equals(TaskStates.ACTIVE))
            {
                dropped(DomainEvent.CREATE);
            }
        }

        public void completed(DomainEvent event)
        {
            status().set(TaskStates.COMPLETED);
        }

        public void dropped(DomainEvent event)
        {
            status().set(TaskStates.DROPPED);
        }
    }

}
