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

package se.streamsource.streamflow.web.application.notification;

import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.event.RemoteEventNotification;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionCollector;
import se.streamsource.streamflow.infrastructure.json.JSONArray;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class ClientNotificationService
    implements EventSourceListener
{
    @Service
    EventSource source;

    List<RemoteEventNotification> clients = new ArrayList<RemoteEventNotification>( );

    public ClientNotificationService( @Service EventSource source )
    {
        this.source = source;

        source.registerListener( this, true );
    }

    public void registerClient( RemoteEventNotification client)
    {
        clients = new ArrayList<RemoteEventNotification>( clients);
        clients.add( client );
    }

    public void eventsAvailable( EventStore source )
    {
        for (RemoteEventNotification client : clients)
        {
            try
            {
                client.notifyEvents();
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }
}
