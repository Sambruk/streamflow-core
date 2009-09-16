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

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * Role for recording the date of creation of the entity.
 */
@Concerns(CreatedOn.CreatedOnLifecycleConcern.class)
@Mixins(CreatedOn.CreatedOnMixin.class)
public interface CreatedOn
{
    interface CreatedOnState
    {
        Property<Date> createdOn();


        void created(DomainEvent event);
    }

    abstract class CreatedOnMixin
            implements CreatedOnState
    {
        public void created(DomainEvent event)
        {
            createdOn().set(event.on().get());
        }
    }

    class CreatedOnLifecycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        @This
        CreatedOnState state;

        public void create() throws LifecycleException
        {
            state.created(DomainEvent.CREATE);
        }

        public void remove() throws LifecycleException
        {
        }
    }
}
