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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.AbstractEventStoreMixin;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * JAVADOC
 */
@Mixins(JdbmEventStoreService.JdbmEventStoreMixin.class)
public interface JdbmEventStoreService
    extends EventStore, EventListener, EventManagement, Activatable, ServiceComposite
{
    class JdbmEventStoreMixin
        extends AbstractEventStoreMixin
        implements EventManagement
    {
        @Service
        FileConfiguration fileConfig;

        private RecordManager recordManager;
        private BTree index;
        private Serializer serializer;
        public File dataFile;

        public void activate() throws IOException
        {
            super.activate();

            dataFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events");
            File directory = dataFile.getAbsoluteFile().getParentFile();
            directory.mkdirs();
            String name = dataFile.getAbsolutePath();
            Properties properties = new Properties();
            properties.put(RecordManagerOptions.AUTO_COMMIT, "false");
            properties.put(RecordManagerOptions.DISABLE_TRANSACTIONS, "false");
            initialize(name, properties);
        }

        public void passivate() throws IOException
        {
            System.out.println("Close event db");
            recordManager.close();
        }

        public void removeAll() throws IOException
        {
            // Delete event files
            passivate();

            new File(dataFile,"events.db").delete();
            new File(dataFile,"events.lg").delete();

            activate();
        }

        public void importEvents(Reader in) throws IOException
        {
            try
            {
                lock.lock();
                String valueJson;
                BufferedReader reader = new BufferedReader(in);
                int count = 0;
                while ((valueJson = reader.readLine()) != null)
                {
                    TransactionEvents transaction = (TransactionEvents) transactionEventsType.fromJSON(valueJson, module);
                    ValueBuilder<TransactionEvents> builder = transaction.buildWith();
                    builder.prototype().timestamp().set(System.currentTimeMillis());
                    storeEvents(transaction);

                    count++;
                    if (count%1000 == 0)
                    {
                        commit(); // Commit every 1000 transactions to avoid OutOfMemory issues
                    }

                }
                commit();
            } catch (JSONException e)
            {
                rollback();
                throw (IOException) new IOException("Could not parse events").initCause(e);
            } finally
            {
                lock.unlock();
            }
        }

        public Iterable<TransactionEvents> events(Date afterDate, final int maxEvents)
        {
            final Long afterTime = afterDate == null ? Long.MIN_VALUE : afterDate.getTime()+1;

            return new Iterable<TransactionEvents>()
            {
                public Iterator<TransactionEvents> iterator()
                {
                    // Lock datastore first
                    lock.lock();

                    try
                    {
                        final TupleBrowser browser = index.browse(afterTime);

                        return new Iterator<TransactionEvents>()
                        {
                            Tuple tuple = new Tuple();
                            int count = 0;

                            TransactionEvents transactionEvents;

                            public boolean hasNext()
                            {
                                try
                                {
                                    if (count >= maxEvents)
                                    {
                                        lock.unlock();
                                        return false;
                                    }

                                    if (browser.getNext(tuple))
                                    {
                                        // Get next transaction
                                        byte[] eventData = (byte[]) tuple.getValue();
                                        String eventJson = new String(eventData, "UTF-8");
                                        JSONTokener tokener = new JSONTokener(eventJson);
                                        JSONObject transaction = (JSONObject) tokener.nextValue();
                                        transactionEvents = (TransactionEvents) transactionEventsType.fromJSON(transaction, module);
                                        return true;
                                    } else
                                    {
                                        lock.unlock();
                                        return false;
                                    }
                                } catch (Exception e)
                                {
                                    lock.unlock();
                                    return false;
                                }
                            }

                            public TransactionEvents next()
                            {
                                return transactionEvents;
                            }

                            public void remove()
                            {
                            }
                        };
                    } catch (IOException e)
                    {
                       return new ArrayList<TransactionEvents>().iterator();
                    }

                }
            };
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

        protected void storeEvents(TransactionEvents transaction)
                throws IOException
        {
            String jsonString = transaction.toJSON();
            index.insert(transaction.timestamp().get(), jsonString.getBytes("UTF-8"), false);
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
