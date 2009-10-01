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

/**
 * Status for a task. Possible transitions are:
 * Active -> Completed, Dropped, Done
 * Done -> Active, Dropped, Completed
 * Completed -> Archived
 * Dropped -> Archived
 */
@Mixins(TaskStatus.TaskStatusMixin.class)
public interface TaskStatus
{
    void complete();
    void done();
    void activate();
    void drop();

    interface TaskStatusState
    {
        @UseDefaults
        Property<TaskStates> status();

        void statusChanged(DomainEvent event, TaskStates status);
    }

    abstract class TaskStatusMixin
            implements TaskStatus, TaskStatusState
    {

        public void complete()
        {
            if (status().get().equals(TaskStates.ACTIVE) || status().get().equals(TaskStates.DONE))
            {
                statusChanged(DomainEvent.CREATE, TaskStates.COMPLETED);
            }
        }

        public void drop()
        {
            if (status().get().equals(TaskStates.ACTIVE)  || status().get().equals(TaskStates.DONE))
            {
                statusChanged(DomainEvent.CREATE, TaskStates.DROPPED);
            }
        }

        public void done()
        {
            if (status().get().equals(TaskStates.ACTIVE))
            {
                statusChanged(DomainEvent.CREATE, TaskStates.DONE);
            }
        }

        public void activate()
        {
            if (status().get().equals(TaskStates.DONE) || status().get().equals(TaskStates.COMPLETED))
            {
                statusChanged(DomainEvent.CREATE, TaskStates.ACTIVE);
            }
        }
    }

}
