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

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Mixins(SharedTaskReceiver.TaskReceiverMixin.class)
public interface SharedTaskReceiver
{
    void receiveTask(Task task);

    class TaskReceiverMixin
            implements SharedTaskReceiver
    {
        @This
        Owner owner;

        public void receiveTask(Task task)
        {
            task.ownedBy(owner);
        }
    }
}
