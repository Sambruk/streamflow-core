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
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(NotificationService.Mixin.class)
public interface NotificationService
        extends ServiceComposite, Activatable
{
    abstract class Mixin
            implements EventSourceListener, NotificationService
    {
        @Service
        EventSource source;

        List<SocketChannel> clientSockets = new ArrayList<SocketChannel>();

        ScheduledExecutorService socketListener;
        public Selector selector;
        public ByteBuffer notifyData;

        public void activate() throws Exception
        {
            notifyData = ByteBuffer.allocateDirect( 10 );

            source.registerListener( this, true );
            socketListener = Executors.newSingleThreadScheduledExecutor();

            selector = Selector.open();
            ServerSocketChannel ssChannel = ServerSocketChannel.open();
            ssChannel.configureBlocking( false );
            int port = 8888;
            ssChannel.socket().bind( new InetSocketAddress( port ) );

            ssChannel.register( selector, SelectionKey.OP_ACCEPT );

            socketListener.scheduleAtFixedRate( new Runnable()
            {
                public void run()
                {
                    try
                    {
 //                       Logger.getLogger( "notification" ).info( "Select socket" );
                        selector.select( 5000 );

                        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                        while (it.hasNext())
                        {
                            Logger.getLogger( "notification" ).info( "Incoming socket" );
                            SelectionKey selectionKey = it.next();
                            it.remove();

                            if (selectionKey.isAcceptable())
                            {
                                ServerSocketChannel ssChannel = (ServerSocketChannel) selectionKey.channel();
                                SocketChannel sChannel = ssChannel.accept();
                                clientSockets.add( sChannel );
                            }
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, 1, 1, TimeUnit.MICROSECONDS );
        }

        public void passivate() throws Exception
        {
            source.unregisterListener( this );

            selector.close();
            socketListener.shutdown();

            for (SocketChannel clientSocket : clientSockets)
            {
                try
                {
                    clientSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void eventsAvailable( EventStore source )
        {
            try
            {
                for (SocketChannel clientSocket : clientSockets)
                {
                    try
                    {
                        notifyData.clear();
                        notifyData.put( (byte) 0xFF );
                        notifyData.flip();
                        clientSocket.write( notifyData );
                    } catch (IOException e)
                    {
                        List<SocketChannel> newList = new ArrayList<SocketChannel>( clientSockets );
                        newList.remove( clientSocket );
                        clientSockets = newList;
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            Logger.getLogger( "notification" ).info( "Notified" + clientSockets.size() + " clients" );
        }
    }
}
