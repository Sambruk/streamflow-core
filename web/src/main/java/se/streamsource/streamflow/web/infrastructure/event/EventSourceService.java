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

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.*;
import jdbm.recman.CacheRecordManager;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.value.ValueCompositeType;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.util.json.JSONStringer;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This service collects indidivual events created during a UoW
 * and publishes them in one go to subscribers, but only if the UoW is
 * completed successfully.
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

        @Uses
        ServiceDescriptor descriptor;

        @Service
        FileConfiguration fileConfig;

        private RecordManager recordManager;
        private BTree index;
        private Serializer serializer;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        EventSource source;

        Map<UnitOfWork, List<DomainEvent>> uows = new HashMap<UnitOfWork, List<DomainEvent>>();

        Map<EventSourceListener, EventSpecification> listeners = new ConcurrentHashMap<EventSourceListener, EventSpecification>();

        List<DomainEvent> currentEvents;
        EventSpecification currentSpecification;


        public void activate() throws Exception
        {
            File dataFile = new File(fileConfig.dataDirectory(), descriptor.identity() + "/events");
            File directory = dataFile.getAbsoluteFile().getParentFile();
            directory.mkdirs();
            String name = dataFile.getAbsolutePath();
            Properties properties = new Properties();
            properties.put(RecordManagerOptions.AUTO_COMMIT, "false");
            properties.put(RecordManagerOptions.DISABLE_TRANSACTIONS, "false");
            initialize(name, properties);
        }

        public void passivate() throws Exception
        {
        }

        public void registerListener(EventSourceListener listener, EventSpecification specification)
        {
            listeners.put(listener, specification);
        }

        public void unregisterListener(EventSourceListener subscriber)
        {
            listeners.remove(subscriber);
        }

        public Iterable<DomainEvent> events(EventSpecification specification, Date startDate, int maxEvents)
        {
            // Current listener wants events
            if (currentEvents != null && specification == currentSpecification )
                return currentEvents;

            return Collections.emptyList(); // TODO Implement this
        }

        public synchronized void notifyEvent(DomainEvent event)
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
/*
                            // Store all events from this UoW as one array
                            JSONStringer json = new JSONStringer();

                            json.array();
                            for (DomainEvent domainEvent : eventList)
                            {
                                ValueCompositeType type = spi.getValueDescriptor(domainEvent).valueType();
                                type.toJSON(domainEvent, json);
                            }
                            json.endArray();

                            recordManager.insert(json.toString().getBytes("UTF-8"), serializer);
*/

                            synchronized (listeners)
                            {
                                for (Map.Entry<EventSourceListener,EventSpecification> listener : listeners.entrySet())
                                {
                                    // Filter events for the source
                                    currentEvents = null;
                                    for (DomainEvent domainEvent : eventList)
                                    {
                                        if (listener.getValue().accept(domainEvent))
                                        {
                                            if (currentEvents == null)
                                                currentEvents = new ArrayList<DomainEvent>();

                                            currentEvents.add(domainEvent);
                                        }
                                    }

                                    if (currentEvents != null)
                                    {
                                        currentSpecification = listener.getValue();
                                        listener.getKey().eventsAvailable(source, listener.getValue());
                                    }

                                    currentEvents = null;
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

        private void initialize(String name, Properties properties)
                throws IOException
        {
            recordManager = RecordManagerFactory.createRecordManager(name, properties);
            serializer = new ByteArraySerializer();
            recordManager = new CacheRecordManager(recordManager, new MRU(1000));
            long recid = recordManager.getNamedObject("index");
            if (recid != 0)
            {
                index = BTree.load(recordManager, recid);
            } else
            {
                ByteArrayComparator comparator = new ByteArrayComparator();
                index = BTree.createInstance(recordManager, comparator, serializer, new LongSerializer(), 16);
                recordManager.setNamedObject("index", index.getRecid());
            }
            recordManager.commit();
        }
    }
}
