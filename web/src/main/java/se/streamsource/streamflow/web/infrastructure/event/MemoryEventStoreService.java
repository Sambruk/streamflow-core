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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
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

        public Iterable<DomainEvent> events(EventSpecification specification, Date afterDate, int maxEvents)
        {
            // Lock datastore first
            lock.lock();
            List<DomainEvent> events = new ArrayList<DomainEvent>();
            try
            {
                Long startTime = afterDate == null ? Long.MIN_VALUE : afterDate.getTime();
                Collection<String> eventsAfterDate = store.tailMap(startTime).values();

                for (String eventJson : eventsAfterDate)
                {
                    JSONTokener tokener = new JSONTokener(eventJson);
                    JSONArray array = (JSONArray) tokener.nextValue();
                    for (int i = 0; i  < array.length(); i++)
                    {
                        JSONObject valueJson = (JSONObject) array.get(i);
                        DomainEvent event = (DomainEvent) domainEventType.fromJSON(valueJson, module);
                        if (event.on().get().after(afterDate) && specification.accept(event))
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