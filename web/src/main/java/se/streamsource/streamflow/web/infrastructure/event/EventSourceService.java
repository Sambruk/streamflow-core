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
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.entity.Identity;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.util.json.*;
import org.qi4j.spi.value.ValueCompositeType;
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

        @Structure
        ModuleSPI module;

        @This
        EventSource source;

        @This
        Identity identity;

        Map<UnitOfWork, List<DomainEvent>> uows = new HashMap<UnitOfWork, List<DomainEvent>>();

        Map<EventSourceListener, EventSpecification> listeners = new ConcurrentHashMap<EventSourceListener, EventSpecification>();

        List<DomainEvent> currentEvents;
        EventSpecification currentSpecification;
        public ValueCompositeType domainEventType;
        public Logger logger;


        public void activate() throws Exception
        {
            logger = Logger.getLogger(identity.identity().get());

            File dataFile = new File(fileConfig.dataDirectory(), descriptor.identity() + "/events");
            File directory = dataFile.getAbsoluteFile().getParentFile();
            directory.mkdirs();
            String name = dataFile.getAbsolutePath();
            Properties properties = new Properties();
            properties.put(RecordManagerOptions.AUTO_COMMIT, "false");
            properties.put(RecordManagerOptions.DISABLE_TRANSACTIONS, "false");
            initialize(name, properties);

            domainEventType = module.valueDescriptor(DomainEvent.class.getName()).valueType();
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
            if (currentEvents != null && specification == currentSpecification)
                return currentEvents;

            // Find events that match the specification and startdate
            List<DomainEvent> events = new ArrayList<DomainEvent>();

            try
            {
                Long startTime = startDate == null ? Long.MIN_VALUE : startDate.getTime();
                TupleBrowser browser = index.browse(startTime);

                Tuple tuple = new Tuple();
                while (browser.getNext(tuple))
                {
                    byte[] eventData = (byte[]) tuple.getValue();
                    String eventJson = new String(eventData, "UTF-8");
                    JSONTokener tokener = new JSONTokener(eventJson);
                    JSONArray array = (JSONArray) tokener.nextValue();
                    for (int i = 0; i  < array.length(); i++)
                    {
                        JSONObject valueJson = (JSONObject) array.get(i);
                        DomainEvent event = (DomainEvent) domainEventType.fromJSON(valueJson, module);
                        if (specification.accept(event))
                            events.add(event);
                    }

                    if (events.size() > maxEvents)
                        break; // Max size has been reached
                }
            } catch (IOException e)
            {
                logger.log(Level.WARNING, "Could not load events", e);
            } catch (JSONException e)
            {
                logger.log(Level.WARNING, "Could not deserialize events", e);
            }

            return events;
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
                        if (eventList.size() > 0)
                        {
                            try
                            {
                                // Store all events from this UoW as one array
                                storeEvents(eventList);
                            } catch (Exception e)
                            {
                                throw new UnitOfWorkCompletionException(e);
                            }
                        }
                    }

                    public void afterCompletion(UnitOfWorkStatus status)
                    {
                        try
                        {
                            if (status.equals(UnitOfWorkStatus.COMPLETED))
                            {
                                if (eventList.size() > 0)
                                {
                                    recordManager.commit();

                                    synchronized (listeners)
                                    {
                                        for (Map.Entry<EventSourceListener, EventSpecification> listener : listeners.entrySet())
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
                            } else
                            {
                                recordManager.rollback();
                            }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        uows.remove(unitOfWork);
                    }
                });
                events = eventList;
                uows.put(unitOfWork, events);
            }
            events.add(event);
        }

        private void storeEvents(List<DomainEvent> eventList)
                throws JSONException, IOException
        {
            JSONStringer json = new JSONStringer();

            json.array();
            for (DomainEvent domainEvent : eventList)
            {
                domainEventType.toJSON(domainEvent, json);
            }
            json.endArray();

            Long timeStamp = eventList.get(0).on().get().getTime();
            String jsonString = json.toString();
            index.insert(timeStamp, jsonString.getBytes("UTF-8"), false);
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
                LongComparator comparator = new LongComparator();
                index = BTree.createInstance(recordManager, comparator,new LongSerializer() , serializer, 16);
                recordManager.setNamedObject("index", index.getRecid());
            }
            recordManager.commit();
        }
    }
}
