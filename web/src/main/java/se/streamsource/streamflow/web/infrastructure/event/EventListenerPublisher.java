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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
@Mixins(EventListenerPublisher.EventListenerPublisherMixin.class)
public interface EventListenerPublisher
    extends EventListener, EventPublisher, ServiceComposite
{
    class EventListenerPublisherMixin
        implements EventListener, EventPublisher
    {
        Map<UnitOfWork, List<DomainEvent>> uows = new HashMap<UnitOfWork, List<DomainEvent>>();

        Set<Writer> subscribers = new HashSet<Writer>();

        @Structure
        UnitOfWorkFactory uowf;

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
                            for (DomainEvent domainEvent : eventList)
                            {
                                String json = domainEvent.toJSON();

                                Set<Writer> disconnected = new HashSet<Writer>();
                                for (Writer subscriber : subscribers)
                                {
                                    try
                                    {
                                        subscriber.write(json);
                                        subscriber.write('\n');
                                        subscriber.flush();
                                    } catch (IOException e)
                                    {
                                        disconnected.add(subscriber);
                                        synchronized (subscriber)
                                        {
                                            subscriber.notify();
                                        }
                                    }
                                }
                                subscribers.removeAll(disconnected);
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

        public void subscribe(Writer writer)
        {
            subscribers.add(writer);
        }
    }
}
