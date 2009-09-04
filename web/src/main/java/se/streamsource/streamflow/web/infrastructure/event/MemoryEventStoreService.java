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
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.spi.util.json.JSONArray;
import org.qi4j.spi.util.json.JSONException;
import org.qi4j.spi.util.json.JSONObject;
import org.qi4j.spi.util.json.JSONTokener;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * In-Memory EventStore. Mainly used for testing.
 */
@Mixins(MemoryEventStoreService.MemoryEventStoreMixin.class)
public interface MemoryEventStoreService
    extends EventStore, EventListener, Activatable, ServiceComposite
{
    class MemoryEventStoreMixin
        extends AbstractEventStoreMixin
    {
        private TreeMap<Long, String> store = new TreeMap<Long, String>();

        public void activate() throws Exception
        {
            super.activate();
        }

        public void passivate() throws Exception
        {
        }

        public Iterable<DomainEvent> events(EventSpecification specification, Date startDate, int maxEvents)
        {
            // Lock datastore first
            lock.lock();
            List<DomainEvent> events = new ArrayList<DomainEvent>();
            try
            {
                Long startTime = startDate == null ? Long.MIN_VALUE : startDate.getTime();
                Collection<String> eventsAfterDate = store.tailMap(startTime).values();

                for (String eventJson : eventsAfterDate)
                {
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
            } catch (JSONException e)
            {
                logger.log(Level.WARNING, "Could not deserialize events", e);
            } finally
            {
                lock.unlock();
            }

            return events;
        }

        protected void rollback()
                throws IOException
        {
        }

        protected void commit()
                throws IOException
        {
        }

        protected void storeEvents(Long timeStamp, String jsonString)
                throws IOException
        {
            store.put(timeStamp, jsonString);
        }
    }
}