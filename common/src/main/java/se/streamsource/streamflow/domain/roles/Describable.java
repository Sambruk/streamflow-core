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
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.Event;
import se.streamsource.streamflow.infrastructure.event.EventCreationConcern;
import se.streamsource.streamflow.infrastructure.event.EventSideEffect;

/**
 * JAVADOC
 */
@Concerns(EventCreationConcern.class)
@SideEffects(EventSideEffect.class)
@Mixins(Describable.DescribableMixin.class)
public interface Describable
{
    void describe(@MaxLength(50) String newDescription);

    boolean hasDescription(String description);

    String getDescription();

    @Mixins(DescribableState.DescribableStateMixin.class)
    interface DescribableState
    {
        @UseDefaults
        Property<String> description();

        @Event
        void described(DomainEvent event, String description);

        abstract class DescribableStateMixin
            implements DescribableState
        {
            @This DescribableState state;

            public void described(DomainEvent event, String description)
            {
                state.description().set(description);
            }
        }
    }

    class DescribableMixin
            implements Describable
    {
        @This
        DescribableState state;

        public void describe(String newDescription)
        {
            state.described(DomainEvent.CREATE, newDescription);
        }

        public boolean hasDescription(String description)
        {
            return state.description().get().equals(description);
        }

        public String getDescription()
        {
            return state.description().get();
        }
    }
}
