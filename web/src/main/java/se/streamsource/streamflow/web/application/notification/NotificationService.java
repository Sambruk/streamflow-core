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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.RemoteEventNotification;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JAVADOC
 */
@Mixins(NotificationService.NotificationServiceMixin.class)
public interface NotificationService
        extends ServiceComposite, Activatable
{
    void registerClient( String id, RemoteEventNotification client );
    void deregisterClient( String id );


    abstract class NotificationServiceMixin
            implements EventSourceListener, NotificationService
    {
        @Service
        EventSource source;

        Map<String, RemoteEventNotification> clients = new HashMap<String, RemoteEventNotification>();

        ExecutorService notifier;

        public void activate() throws Exception
        {
            source.registerListener( this, true );
            notifier = Executors.newSingleThreadExecutor();
        }

        public void passivate() throws Exception
        {
            source.unregisterListener( this );
            notifier.shutdown();
        }

        public void registerClient( String id, RemoteEventNotification client )
        {
            Map<String, RemoteEventNotification> newClients = new HashMap<String, RemoteEventNotification>( clients );
            newClients.put( id, client );
            clients = newClients;
        }

        public void deregisterClient( String id )
        {
            Map<String, RemoteEventNotification> newClients = new HashMap<String, RemoteEventNotification>( clients );
            newClients.remove( id );
            clients = newClients;
        }

        public void eventsAvailable( EventStore source )
        {
            for (RemoteEventNotification client : clients.values())
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
}
