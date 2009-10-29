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
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.resource.EventsClientResource;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * JAVADOC
 */
@Mixins(ClientEventNotificationService.Mixin.class)
public interface ClientEventNotificationService
        extends Activatable, ServiceComposite
{
    public class Mixin
            implements Activatable, Runnable
    {
        @Structure
        ValueBuilderFactory vbf;

        @Service
        EventSourceListener listener;

        AccountSelector selector;

        private EventsClientResource resource;

        ScheduledExecutorService notificationListener = Executors.newSingleThreadScheduledExecutor();
        public SocketChannel channel;

        public Mixin( @Service StreamFlowApplication app )
        {
            selector = app.getAccountSelector();
        }

        public void activate() throws Exception
        {
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

        }

        public void passivate() throws Exception
        {
            if (resource != null)
            {
                System.out.println("DEREGISTER CLIENT");
                deregisterClient();
            }
        }

        private void registerClient( AccountModel accountModel )
        {
            Reference ref = new Reference( accountModel.settings().server().get() );
            String host = ref.getHostDomain();

            try
            {
                channel = SocketChannel.open();
                channel.configureBlocking( true );

                // Send a connection request to the server; this method is non-blocking
                channel.connect( new InetSocketAddress( host, 8888 ) );

                while (!channel.finishConnect())
                {
                    try
                    {
                        Thread.sleep( 10 );
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                resource = accountModel.serverResource().events();
                notificationListener.scheduleAtFixedRate( this, 1, 1, TimeUnit.MILLISECONDS );
            } catch (IOException e)
            {
                throw new OperationException( StreamFlowResources.could_not_register_client, e);
            }
        }

        private void deregisterClient()
        {
            if (resource != null)
            {
                try
                {
                    channel.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                notificationListener.shutdown();
                channel = null;

                resource = null;
            }
        }

        public void run()
        {
            ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
            try
            {
                // Clear the buffer and read bytes from socket
                buf.clear();
                int numBytesRead = channel.read( buf );

                if (numBytesRead == -1)
                {
                    // No more bytes can be read from the channel
                    channel.close();
                    notificationListener.shutdown();
                } else
                {
                    // To read the bytes, flip the buffer
                    buf.flip();

                    pollEvents();
                }
            } catch (IOException e)
            {
                // Connection may have been closed
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
    }
}
