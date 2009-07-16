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

import java.util.Date;

/**
 * JAVADOC
 */
@Concerns(Assignable.AssignOnStatusChangeConcern.class)
@Mixins(Assignable.AssignableMixin.class)
public interface Assignable
{
    void assignTo(Assignee assignee);

    interface AssignableState
    {
        @Optional
        Association<Assignee> assignedTo();

        @Optional
        Property<Date> assignedOn();
    }

    class AssignableMixin
            implements Assignable
    {
        @This
        AssignableState state;

        public void assignTo(Assignee assignee)
        {
            if (!assignee.equals(state.assignedTo().get()))
            {
                state.assignedTo().set(assignee);
                state.assignedOn().set(new Date());
            }
        }
    }

    abstract class AssignOnStatusChangeConcern
        extends ConcernOf<TaskStatus>
        implements TaskStatus
    {
        @This
        AssignableState state;

        @This
        Assignable assignable;

        public void completedBy(Assignee assignee)
        {
            ensureAssigned(assignee);

            next.completedBy(assignee);
        }

        public void droppedBy(Assignee assignee)
        {
            ensureAssigned(assignee);

            next.droppedBy(assignee);
        }

        private void ensureAssigned(Assignee assignee)
        {
            if (state.assignedTo().get() == null)
                assignable.assignTo(assignee);
        }
    }

}
