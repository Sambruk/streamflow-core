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
 * Delegations of tasks
 */
@Mixins(Delegations.DelegationsMixin.class)
public interface Delegations
{
    void accept(Task task, Assignee assignee);
    void reject(Task task);
    void completeDelegatedTask(Task task, Assignee assignee);

    class DelegationsMixin
        implements Delegations
    {
        @This
        Owner owner;

        public void accept(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
            task.ownedBy(owner);
        }

        public void reject(Task task)
        {
            task.rejectDelegation();
        }

        public void completeDelegatedTask(Task task, Assignee assignee)
        {
            task.completedBy(assignee);
        }
    }
}
