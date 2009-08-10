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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.sideeffect.GenericSideEffect;

import java.lang.reflect.Method;

/**
 * Generate event
 */
@AppliesTo(Event.class)
public class EventSideEffect
    extends GenericSideEffect
{
    @Service
    EventStore eventStore;

    @Override
    protected void invoke(Method method, Object[] args) throws Throwable
    {
        if (args[0] != null && DomainEvent.class.equals(method.getParameterTypes()[0]))
        {
            DomainEvent event = (DomainEvent) args[0];
            eventStore.storeEvent(event);
        }
    }
}
