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
import jdbm.helper.ByteArraySerializer;
import jdbm.helper.LongComparator;
import jdbm.helper.LongSerializer;
import jdbm.helper.MRU;
import jdbm.helper.Serializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import jdbm.recman.CacheRecordManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * JAVADOC
 */
@Mixins(JdbmEventStoreService.JdbmEventStoreMixin.class)
public interface JdbmEventStoreService
    extends EventStore, EventListener, Activatable, ServiceComposite
{
    class JdbmEventStoreMixin
        extends AbstractEventStoreMixin
    {
        @Service
        FileConfiguration fileConfig;

        private RecordManager recordManager;
        private BTree index;
        private Serializer serializer;

        public void activate() throws Exception
        {
            super.activate();

            File dataFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events");
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
            recordManager.close();
        }

        public Iterable<DomainEvent> events(EventSpecification specification, Date startDate, int maxEvents)
        {
            // Lock datastore first
            lock.lock();
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
            } finally
            {
                lock.unlock();
            }

            return events;
        }

        protected void rollback()
                throws IOException
        {
            recordManager.rollback();
        }

        protected void commit()
                throws IOException
        {
            recordManager.commit();
        }

        protected void storeEvents(Long timeStamp, String jsonString)
                throws IOException
        {
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
            commit();
        }
    }
}
