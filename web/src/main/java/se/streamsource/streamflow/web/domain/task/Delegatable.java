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

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.task.TaskStates;

import java.util.Date;

/**
 * JAVADOC
 */
@Concerns(Delegatable.UnreadOnStatusChangeConcern.class)
@Mixins(Delegatable.DelegatableMixin.class)
public interface Delegatable
{
    void delegateTo(Delegatee delegatee);

    void rejectDelegation();

    interface DelegatableState
    {
        @Optional
        Association<Delegatee> delegatedTo();

        @Optional
        Property<Date> delegatedOn();
    }

    class DelegatableMixin
        implements Delegatable
    {
        @This
        DelegatableState state;

        public void delegateTo(Delegatee delegatee)
        {
            state.delegatedTo().set(delegatee);
            state.delegatedOn().set(new Date());
        }

        public void rejectDelegation()
        {
            state.delegatedTo().set(null);
            state.delegatedOn().set(null);
        }
    }

    abstract class UnreadOnStatusChangeConcern
        extends ConcernOf<TaskStatus>
        implements TaskStatus
    {
        @This DelegatableState state;
        @This TaskStatusState status;
        @This IsRead isRead;

        public void complete()
        {
            next.complete();

            if (state.delegatedTo().get() != null && !status.status().get().equals(TaskStates.ACTIVE))
            {
                isRead.markAsUnread();
            }
        }

        public void drop()
        {
            next.drop();

            if (state.delegatedTo().get() != null && !status.status().get().equals(TaskStates.ACTIVE))
            {
                isRead.markAsUnread();
            }
        }
    }
}