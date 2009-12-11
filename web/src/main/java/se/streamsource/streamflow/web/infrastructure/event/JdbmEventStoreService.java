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
import se.streamsource.streamflow.infrastructure.event.AbstractEventStoreMixin;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

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
        private File dataFile;
        private File databaseFile;
        private File logFile;

        public void activate() throws IOException
        {
            super.activate();

            dataFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events");
            databaseFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events.db");
            logFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events.lg");
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
            logger.info("Close event db");
            recordManager.close();
        }

        public void removeAll() throws IOException
        {
            // Delete event files
            passivate();

            if (!databaseFile.delete())
                throw new IOException("Could not delete event database");
            if (!logFile.delete())
                throw new IOException("Could not delete event log");

            activate();
        }

        public void removeTo( Date date ) throws IOException
        {
            lock.lock();

            try
            {
                final TupleBrowser browser = index.browse();
                Tuple tuple = new Tuple();
                while (browser.getNext( tuple ))
                {
                    Long key = (Long) tuple.getKey();
                    if (key <= date.getTime())
                    {
                        index.remove( key );
                    } else
                    {
                        break; // We're done
                    }
                }
                recordManager.commit();
            } catch (IOException ex)
            {
                recordManager.rollback();
            } finally
            {
                lock.unlock();
            }
        }

        public void importEvents(Reader in) throws IOException
        {
            try
            {
                lock.lock();
                String valueJson;
                BufferedReader reader = new BufferedReader(in);
                int count = 0;
                logger.info( "Import events" );
                while ((valueJson = reader.readLine()) != null)
                {
                    JSONObject json = (JSONObject) new JSONTokener(valueJson).nextValue();
                    TransactionEvents transaction = (TransactionEvents) transactionEventsType.fromJSON(json, module);
                    ValueBuilder<TransactionEvents> builder = transaction.buildWith();
                    builder.prototype().timestamp().set(getCurrentTimestamp());
                    transaction = builder.newInstance();
                    storeEvents(transaction);

                    count++;
                    if (count%1000 == 0)
                    {
                        logger.info( "Imported "+count+" events" );
                        commit(); // Commit every 1000 transactions to avoid OutOfMemory issues
                    }

                }
                commit();
                logger.info("Import events done, "+count+" events imported");
            } catch (JSONException e)
            {
                rollback();
                throw (IOException) new IOException("Could not parse events").initCause(e);
            } finally
            {
                lock.unlock();
            }
        }

        public void transactionsAfter( long afterTimestamp, TransactionHandler handler )
        {
            // Lock datastore first
            lock.lock();

            final Long afterTime = afterTimestamp+1;

            try
            {
                final TupleBrowser browser = index.browse(afterTime);

                Tuple tuple = new Tuple();

                while (browser.getNext( tuple ))
                {
                    // Get next transaction
                    TransactionEvents events  = readTransactionEvents(tuple);

                    if (!handler.handleTransaction( events ))
                    {
                        return;
                    }
                }
            } catch (Exception e)
            {
                logger.log( Level.WARNING, "Could not iterate transactions", e );
            } finally
            {
                lock.unlock();
            }
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

        private TransactionEvents readTransactionEvents(Tuple tuple)
                throws UnsupportedEncodingException, JSONException
        {
            byte[] eventData = (byte[]) tuple.getValue();
            String eventJson = new String(eventData, "UTF-8");
            JSONTokener tokener = new JSONTokener(eventJson);
            JSONObject transaction = (JSONObject) tokener.nextValue();
            return (TransactionEvents) transactionEventsType.fromJSON(transaction, module);
        }
    }
}
