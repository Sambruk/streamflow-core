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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(EventStoreService.EventStoreMixin.class)
public interface EventStoreService
    extends EventStore, ServiceComposite, Activatable
{
    class EventStoreMixin
        implements EventStore, Activatable
    {
        Map<UnitOfWork, List<DomainEvent>> uows = new HashMap<UnitOfWork, List<DomainEvent>>();

        @Structure
        UnitOfWorkFactory uowf;

        public void activate() throws Exception
        {

        }

        public void passivate() throws Exception
        {
        }

        public synchronized void storeEvent(DomainEvent event)
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
                            System.out.println("EVENTS:");
                            for (DomainEvent domainEvent : eventList)
                            {
                                System.out.print(domainEvent.toJSON());
                                System.out.print("\n");
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
