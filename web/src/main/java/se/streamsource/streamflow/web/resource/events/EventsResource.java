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
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.infrastructure.event.RemoteEventNotification;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;
import se.streamsource.streamflow.web.application.notification.ClientNotificationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.Date;

/**
 * JAVADOC
 */
public class EventsResource
        extends ServerResource
{
    @Service
    EventSource source;

    @Service
    ClientNotificationService clientNotification;

    public EventsResource()
    {
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    protected Representation post( Representation representation, Variant variant ) throws ResourceException
    {
        try
        {
            InputStream in = representation.getStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            RemoteEventNotification stub = (RemoteEventNotification) oin.readObject();

            clientNotification.registerClient( stub );
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return new EmptyRepresentation();
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        String after = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "after" );

        final Date afterDate = after == null ? null : new Date(Long.parseLong(after ));

        return new WriterRepresentation(MediaType.TEXT_PLAIN, 1000)
        {
            public void write(Writer writer) throws IOException
            {
                source.registerListener(new EventSubscriberWriter(writer, afterDate));

                try
                {
                    synchronized (writer)
                    {
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
        private Date afterDate;

        EventSubscriberWriter( Writer writer, Date afterDate )
        {
            this.writer = writer;
            this.afterDate = afterDate;
        }

        public void eventsAvailable(EventStore eventStore)
        {
            eventStore.transactions( afterDate, new TransactionHandler()
            {
                public boolean handleTransaction( TransactionEvents transaction )
                {
                    try
                    {
                        writer.write(transaction.toJSON());
                        writer.write('\n');
                        writer.flush();

                        return true;
                    } catch (IOException e)
                    {
                        synchronized (writer)
                        {
                            writer.notify();
                        }

                        source.unregisterListener( EventSubscriberWriter.this );
                        return false;
                    }
                }
            });
        }
    }
}
