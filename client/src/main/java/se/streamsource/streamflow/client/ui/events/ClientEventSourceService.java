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

import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ClientEventSourceService.ClientEventSourceMixin.class)
public interface ClientEventSourceService
        extends EventSource, EventSourceListener, ServiceComposite
{
    class ClientEventSourceMixin
            implements EventSource, EventSourceListener, Activatable, EventStore
    {
        public Reader reader;
        public Iterable<TransactionEvents> events;

        public void activate() throws Exception
        {
        }

        public void passivate() throws Exception
        {
        }

        private List<Reference<EventSourceListener>> listeners = new ArrayList<Reference<EventSourceListener>>();

        // EventSource implementation

        public void registerListener(EventSourceListener subscriber, boolean asynchronous)
        {
            // Ignore asynch for now
            listeners.add(new WeakReference<EventSourceListener>(subscriber));
        }

        public void registerListener(EventSourceListener listener)
        {
            registerListener(listener, false);
        }

        public void unregisterListener(EventSourceListener subscriber)
        {
            Iterator<Reference<EventSourceListener>> referenceIterator = listeners.iterator();
            while (referenceIterator.hasNext())
            {
                Reference<EventSourceListener> eventSourceListenerReference = referenceIterator.next();
                EventSourceListener lstnr = eventSourceListenerReference.get();
                if (lstnr == null || lstnr.equals(subscriber))
                {
                    referenceIterator.remove();
                    return;
                }
            }
        }

        // EventSourceListener implementation
        public void eventsAvailable(EventStore source)
        {
            events = source.events(null, Integer.MAX_VALUE);

            Iterator<Reference<EventSourceListener>> referenceIterator = listeners.iterator();
            while (referenceIterator.hasNext())
            {
                Reference<EventSourceListener> eventSourceListenerReference = referenceIterator.next();
                EventSourceListener lstnr = eventSourceListenerReference.get();
                if (lstnr == null)
                {
                    referenceIterator.remove();
                } else
                {
                    lstnr.eventsAvailable(this);
                }
            }
        }

        // EventStore implementation
        public Iterable<TransactionEvents> events(@Optional Date startDate, int maxEvents)
        {
            return events;
        }
    }
}
