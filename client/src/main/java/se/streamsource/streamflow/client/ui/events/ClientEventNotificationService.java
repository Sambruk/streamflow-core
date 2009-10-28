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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.resource.EventsClientResource;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.event.RemoteEventNotification;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Random;


/**
 * JAVADOC
 */
@Mixins(ClientEventNotificationService.ClientEventNotificationMixin.class)
public interface ClientEventNotificationService
        extends Activatable, ServiceComposite
{
    public class ClientEventNotificationMixin
            implements Activatable
    {
        @Structure
        ValueBuilderFactory vbf;

        @Service
        EventSourceListener listener;

        AccountSelector selector;

        private Remote stub;
        private RemoteEventNotification remoteNotification;

        private EventsClientResource resource;

        String id;

        public ClientEventNotificationMixin( @Service StreamFlowApplication app )
        {
            selector = app.getAccountSelector();
        }

        public void activate() throws Exception
        {

            id = Math.abs( new Random().nextLong() ) + "";

            remoteNotification = new RemoteEventNotificationImpl2();
            try
            {
                stub = UnicastRemoteObject.exportObject( remoteNotification, 0 );

                selector.addListSelectionListener( new ListSelectionListener()
                {
                    public void valueChanged( ListSelectionEvent e )
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            AccountModel accountModel = selector.getSelectedAccount();
                            if (accountModel != null)
                            {
                                deregisterClient();
                                registerClient( accountModel );
                            } else
                            {
                                deregisterClient();
                            }
                        }
                    }
                } );
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        public void passivate() throws Exception
        {
            if (resource != null)
            {
                deregisterClient();
            }
        }

        private void registerClient( AccountModel accountModel )
        {
            try
            {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream( bytes );
                oout.writeObject( stub );
                oout.close();
                InputStream in = new ByteArrayInputStream( bytes.toByteArray() );
                resource = accountModel.serverResource().events();
                resource.registerClient( id, in );
            } catch (IOException e1)
            {
                e1.printStackTrace();
            } catch (ResourceException e1)
            {
                e1.printStackTrace();
            }
        }

        private void deregisterClient()
        {
            if (resource != null)
            {
                try
                {
                    resource.deregisterClient( id );
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }

                resource = null;
            }
        }

        public void pollEvents()
        {
            if (resource != null)
            {
                listener.eventsAvailable( new EventStore()
                {
                    public void transactions( @Optional Date afterTimestamp, TransactionHandler handler )
                    {
                        try
                        {
                            Representation eventsRepresentation = resource.getEvents( afterTimestamp );

                            BufferedReader reader = new BufferedReader( eventsRepresentation.getReader() );

                            try
                            {
                                String line;
                                while ((line = reader.readLine()) != null)
                                {
                                    TransactionEvents events = vbf.newValueFromJSON( TransactionEvents.class, line );
                                    if (!handler.handleTransaction( events ))
                                    {
                                        reader.close();
                                        return;
                                    }
                                }
                                reader.close();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        } catch (ResourceException e)
                        {
                            e.printStackTrace();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                } );

            }
        }


        public class RemoteEventNotificationImpl2
                implements RemoteEventNotification
        {
            @Service
            ClientEventNotificationService notification;

            @Service
            EventSourceListener esl;

            public void notifyEvents() throws RemoteException
            {
                pollEvents();
            }
        }
    }
}
