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

package se.streamsource.streamflow.infrastructure.event.source;

import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Takes a list of DomainEvents and filters them according to a given event specification.
 */
public class EventHandlerFilter
    implements EventHandler
{
    private EventHandler handler;
    private EventSpecification eventSpecification;

    public EventHandlerFilter( EventSpecification eventSpecification, EventHandler handler )
    {
        this.handler = handler;
        this.eventSpecification = eventSpecification;
    }

    public boolean handleEvent( DomainEvent event )
    {
        if (eventSpecification.accept( event ))
            return handler.handleEvent( event );
        else
            return true;
    }
}