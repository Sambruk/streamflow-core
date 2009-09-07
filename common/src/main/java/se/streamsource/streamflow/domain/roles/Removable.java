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

package se.streamsource.streamflow.domain.roles;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.Event;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Generic interface for removing objects. They are not
 * physically removed, but are instead only marked as removed.
 * All state still exists and can be queried.
 *
 * Queries for entities that include this one should ensure that the removed flag is set to false
 * before allowing it to be included.
 */
@Mixins(Removable.RemovableMixin.class)
public interface Removable
{
    /**
     * Mark the entity as removed
     *
     * @return true if the entity was removed. False if the entity was already marked as removed
     */
    boolean removeEntity();

    /**
     * Mark the entity as not-removed
     *
     * @return true if the entity was reinstate. False if the entity was already active.
     */
    boolean reinstate();

    interface RemovableState
    {
        @UseDefaults
        Property<Boolean> removed();

        @Event
        void removedChanged(DomainEvent event, boolean isRemoved);
    }

    class RemovableMixin
            implements Removable
    {
        @This RemovableState state;

        public boolean removeEntity()
        {
            if (!state.removed().get())
            {
                state.removedChanged(DomainEvent.CREATE, true);
                return true;
            } else
            {
                return false;
            }
        }

        public boolean reinstate()
        {
            if (state.removed().get())
            {
                state.removedChanged(DomainEvent.CREATE, false);
                return true;
            } else
            {
                return false;
            }
        }
    }
}
