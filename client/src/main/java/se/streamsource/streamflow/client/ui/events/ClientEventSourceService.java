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

package se.streamsource.streamflow.client.ui.events;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.common.Optional;
import se.streamsource.streamflow.client.resource.EventsClientResource;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Map;
import java.util.Date;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins(ClientEventSourceService.ClientEventSourceMixin.class)
public interface ClientEventSourceService
        extends EventSource, EventListener, ServiceComposite
{
    class ClientEventSourceMixin
            implements EventSource, EventListener, Activatable, EventStore
    {
        public Reader reader;
        public Iterable<DomainEvent> events;

        public void activate() throws Exception
        {
        }

        public void passivate() throws Exception
        {
        }

        private Map<EventSourceListener, EventSpecification> listeners = new ConcurrentHashMap<EventSourceListener, EventSpecification>();

        // EventSource implementation

        public void registerListener(EventSourceListener subscriber, EventSpecification specification, boolean asynchronous)
        {
            // Ignore asynch for now
            listeners.put(subscriber, specification);
        }

        public void registerListener(EventSourceListener listener, EventSpecification specification)
        {
            registerListener(listener, specification, false);
        }

        public void unregisterListener(EventSourceListener subscriber)
        {
            listeners.remove(subscriber);
        }

        // EventListener implementation
        public void notifyEvent(DomainEvent event)
        {
            events = Collections.singletonList(event);

            for (Map.Entry<EventSourceListener, EventSpecification> listener : listeners.entrySet())
            {
                if (listener.getValue().accept(event))
                    listener.getKey().eventsAvailable(this, listener.getValue());
            }
        }

        // EventStore implementation
        public Iterable<DomainEvent> events(@Optional EventSpecification specification, @Optional Date startDate, int maxEvents)
        {
            return events;
        }
    }
}
