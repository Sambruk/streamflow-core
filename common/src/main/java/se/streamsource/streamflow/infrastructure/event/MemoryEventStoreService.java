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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
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

        public void activate() throws IOException
        {
            super.activate();
        }

        public void passivate() throws Exception
        {
        }

        public Iterable<TransactionEvents> events(Date afterDate, int maxEvents)
        {
            // Lock datastore first
            lock.lock();
            List<TransactionEvents> transactions = new ArrayList<TransactionEvents>();
            try
            {
                Long startTime = afterDate == null ? Long.MIN_VALUE : afterDate.getTime();
                Collection<String> txsAfterDate = store.tailMap(startTime+1).values();

                for (String txJson : txsAfterDate)
                {
                    JSONTokener tokener = new JSONTokener(txJson);
                    JSONObject json = (JSONObject) tokener.nextValue();
                    TransactionEvents tx = (TransactionEvents) transactionEventsType.fromJSON(json, module);
                    transactions.add(tx);

                    if (transactions.size() == maxEvents)
                        break; // Max size has been reached
                }
            } catch (JSONException e)
            {
                logger.log(Level.WARNING, "Could not deserialize events", e);
            } finally
            {
                lock.unlock();
            }

            return transactions;
        }

        protected void rollback()
                throws IOException
        {
        }

        protected void commit()
                throws IOException
        {
        }

        protected void storeEvents(TransactionEvents transaction)
                throws IOException
        {
            String jsonString = transaction.toString();
            store.put(transaction.timestamp().get(), jsonString);
        }
    }
}