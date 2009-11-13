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
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;
import se.streamsource.streamflow.web.application.notification.NotificationService;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * Get events since a given date
 */
public class EventsServerResource
        extends ServerResource
{
    @Service
    EventStore store;

    @Service
    NotificationService notification;

    public EventsServerResource()
    {
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        String after = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "after" );

        final Date afterDate = after == null ? null : new Date(Long.parseLong(after ));

        WriterRepresentation representation = new WriterRepresentation( MediaType.TEXT_PLAIN )
        {
            public void write( final Writer writer ) throws IOException
            {
                store.transactions( afterDate, new TransactionHandler()
                {
                    public boolean handleTransaction( TransactionEvents transaction )
                    {
                        try
                        {
                            String json = transaction.toJSON();
                            writer.write( json );
                            writer.write( '\n' );

                            return true;
                        } catch (IOException e)
                        {
                            return false;
                        }
                    }
                } );
            }
        };
        representation.setCharacterSet( CharacterSet.UTF_8 );
        return representation;
    }
}
