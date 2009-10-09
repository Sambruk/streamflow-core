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

package se.streamsource.streamflow.web.infrastructure.event;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionCollector;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import java.util.ArrayList;
import static java.util.Collections.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This service collects indidivual events created during a UoW
 * and publishes them in one go to listeners, but only if the UoW is
 * completed successfully.
 *
 * The listeners are notified of events asynchronously. They may choose whether
 * to call the provided EventStore or to ignore it.
 */
@Mixins(EventSourceService.EventSourceMixin.class)
public interface EventSourceService
        extends EventListener, EventSource, Activatable, ServiceComposite
{
    class EventSourceMixin
            implements EventListener, EventSource, Activatable
    {
        @Structure
        Qi4jSPI spi;

        @Service
        EventStore eventStore;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        ModuleSPI module;

        @This
        Identity identity;

        private Map<UnitOfWork, List<DomainEvent>> uows = new ConcurrentHashMap<UnitOfWork, List<DomainEvent>>();

        private List<EventSourceListener> listeners = synchronizedList(new ArrayList<EventSourceListener>());

        private Logger logger;
        private ExecutorService eventNotifier;


        public void activate() throws Exception
        {
            logger = Logger.getLogger(identity.identity().get());
            eventNotifier = Executors.newSingleThreadExecutor();
        }

        public void passivate() throws Exception
        {
            eventNotifier.shutdown();
        }

        // EventSource implementation

        public void registerListener(EventSourceListener listener, boolean asynchronous)
        {
            if (asynchronous)
            {
                listener = new AsynchronousListener(listener);
            }

            listeners.add(listener);
        }

        public void registerListener(EventSourceListener listener)
        {
            registerListener(listener, true);
        }

        public void unregisterListener(EventSourceListener subscriber)
        {
            if (!listeners.remove(subscriber))
            {
                listeners.remove(new AsynchronousListener(subscriber));
            }
        }

        public void notifyEvent(DomainEvent event)
        {
            final UnitOfWork unitOfWork = uowf.currentUnitOfWork();
            List<DomainEvent> events = uows.get(unitOfWork);
            if (events == null)
            {
                final List<DomainEvent> eventList = new ArrayList<DomainEvent>();
                unitOfWork.addUnitOfWorkCallback(new UnitOfWorkCallback()
                {
                    public void beforeCompletion() throws UnitOfWorkCompletionException
                    {
                    }

                    public void afterCompletion(UnitOfWorkStatus status)
                    {
                        if (status.equals(UnitOfWorkStatus.COMPLETED))
                        {
                            if (eventList.size() > 0)
                            {
                                ValueBuilder<TransactionEvents> builder = vbf.newValueBuilder(TransactionEvents.class);
                                builder.prototype().timestamp().set(System.currentTimeMillis());
                                builder.prototype().events().set(eventList);
                                final TransactionEvents transaction = builder.newInstance();

                                synchronized (listeners)
                                {
                                    for (final EventSourceListener listener : listeners)
                                    {

                                        listener.eventsAvailable(new EventStore()
                                                {
                                                    public void transactions(@Optional Date startDate, TransactionHandler handler)
                                                    {
                                                        if (startDate == null)
                                                            handler.handleTransaction( transaction );
                                                        else // Delegate to store
                                                            eventStore.transactions( startDate, handler );
                                                    }
                                        });
                                    }
                                }
                            }
                        }

                        uows.remove(unitOfWork);
                    }
                });
                events = eventList;
                uows.put(unitOfWork, events);
            }
            events.add(event);
        }

        class AsynchronousListener
            implements EventSourceListener
        {
            private EventSourceListener listener;

            AsynchronousListener(EventSourceListener listener)
            {
                this.listener = listener;
            }

            public EventSourceListener getListener()
            {
                return listener;
            }

            public void eventsAvailable( final EventStore source)
            {
                final TransactionCollector transactionCollector = new TransactionCollector();
                source.transactions( null, transactionCollector );

                eventNotifier.execute(new Runnable()
                {
                    public void run()
                    {
                        listener.eventsAvailable( new EventStore()
                        {
                            public void transactions( @Optional Date afterTimestamp, TransactionHandler handler )
                            {
                                if (afterTimestamp == null)
                                {
                                    for (TransactionEvents transactionEvents : transactionCollector.transactions())
                                    {
                                        if (!handler.handleTransaction( transactionEvents ))
                                            break;

                                    }
                                } else // Delegate to store
                                    eventStore.transactions(afterTimestamp, handler);
                            }
                        });
                    }
                });
            }

            @Override
            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                AsynchronousListener that = (AsynchronousListener) o;

                if (!listener.equals(that.listener)) return false;

                return true;
            }

            @Override
            public int hashCode()
            {
                return listener.hashCode();
            }
        }
    }
}
