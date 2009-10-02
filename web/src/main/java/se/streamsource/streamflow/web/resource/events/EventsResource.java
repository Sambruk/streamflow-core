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

package se.streamsource.streamflow.web.resource.events;

import org.qi4j.api.injection.scope.Service;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.IOException;
import java.io.Writer;

/**
 * JAVADOC
 */
public class EventsResource
        extends ServerResource
{
    @Service
    EventSource source;

    public EventsResource()
    {
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        return new WriterRepresentation(MediaType.TEXT_PLAIN, 1000)
        {
            public void write(Writer writer) throws IOException
            {
                source.registerListener(new EventSubscriberWriter(writer));

                try
                {
                    synchronized (writer)
                    {
                        writer.write("Event start");
                        writer.flush();
                        writer.wait();
                    }
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    class EventSubscriberWriter
            implements EventSourceListener
    {
        Writer writer;

        EventSubscriberWriter(Writer writer)
        {
            this.writer = writer;
        }

        public void eventsAvailable(EventStore eventStore)
        {
            Iterable<DomainEvent> events = EventFilter.ALL_EVENTS.events(eventStore.events(null, 100000));

            try
            {

                for (DomainEvent event : events)
                {
                    writer.write(event.toJSON());
                    writer.write('\n');
                    writer.flush();
                }
            } catch (IOException e)
            {
                source.unregisterListener(this);
                synchronized (writer)
                {
                    writer.notify();
                }
            }
        }
    }
}
