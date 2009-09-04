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
import org.qi4j.api.entity.Identity;
import org.qi4j.api.common.Optional;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;

import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ModuleSPI module;

        @This
        Identity identity;

        private Map<UnitOfWork, List<DomainEvent>> uows = new ConcurrentHashMap<UnitOfWork, List<DomainEvent>>();

        private Map<EventSourceListener, EventSpecification> listeners = new ConcurrentHashMap<EventSourceListener, EventSpecification>();

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
        public void registerListener(EventSourceListener listener, EventSpecification specification)
        {
            listeners.put(listener, specification);
        }

        public void unregisterListener(EventSourceListener subscriber)
        {
            listeners.remove(subscriber);
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
                                synchronized (listeners)
                                {
                                    for (final Map.Entry<EventSourceListener, EventSpecification> listener : listeners.entrySet())
                                    {
                                        // Filter events for the source
                                        List<DomainEvent> currentEvents = null;
                                        for (DomainEvent domainEvent : eventList)
                                        {
                                            if (listener.getValue().accept(domainEvent))
                                            {
                                                if (currentEvents == null)
                                                    currentEvents = new ArrayList<DomainEvent>();

                                                currentEvents.add(domainEvent);
                                            }
                                        }

                                        // There are events that match the specification for this listener
                                        if (currentEvents != null)
                                        {
                                            final List<DomainEvent> listenerEvents = currentEvents;
                                            final EventSpecification currentSpecification = listener.getValue();
                                            final EventSourceListener esl = listener.getKey();
                                            // Notify listener asynchronously
                                            eventNotifier.execute(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    esl.eventsAvailable(new EventStore()
                                                    {
                                                        public Iterable<DomainEvent> events(@Optional EventSpecification specification, @Optional Date startDate, int maxEvents)
                                                        {
                                                            if (specification == currentSpecification)
                                                                return listenerEvents;
                                                            else // Delegate to store
                                                                return eventStore.events(specification, startDate, maxEvents);
                                                        }
                                                    }, currentSpecification);
                                                }
                                            });
                                        }
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
    }
}
