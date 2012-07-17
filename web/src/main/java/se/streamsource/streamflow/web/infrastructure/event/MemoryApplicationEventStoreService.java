/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.infrastructure.event;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.AbstractApplicationEventStoreMixin;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStore;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * In-Memory ApplicationEventStore. Mainly used for testing.
 */
@Mixins(MemoryApplicationEventStoreService.Mixin.class)
public interface MemoryApplicationEventStoreService
        extends ApplicationEventSource, ApplicationEventStore, ApplicationEventStream, Activatable, ServiceComposite
{
   abstract class Mixin
           extends AbstractApplicationEventStoreMixin
           implements ApplicationEventSource
   {
      // This holds all transactions
      private TreeMap<Long, String> store = new TreeMap<Long, String>();

      public void activate() throws IOException
      {
         super.activate();
      }

      public void passivate() throws Exception
      {
         super.passivate();
      }

      public Input<TransactionApplicationEvents, IOException> transactionsAfter(final long afterTimestamp, final long maxTransactions)
      {
         return new Input<TransactionApplicationEvents, IOException>()
         {
            public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super TransactionApplicationEvents, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
            {
               // Lock datastore first
               lock.lock();
               try
               {
                  output.receiveFrom(new Sender<TransactionApplicationEvents, IOException>()
                  {
                     public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super TransactionApplicationEvents, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                     {
                        try
                        {

                           long count = 0;
                           Long startTime = afterTimestamp + 1;
                           Collection<String> txsAfterDate = store.tailMap(startTime).values();

                           for (String txJson : txsAfterDate)
                           {
                              JSONTokener tokener = new JSONTokener(txJson);
                              JSONObject json = (JSONObject) tokener.nextValue();
                              TransactionApplicationEvents tx = (TransactionApplicationEvents) transactionEventsType.fromJSON(json, module);

                              receiver.receive(tx);

                              count++;
                              if (count == maxTransactions)
                                 return;
                           }
                        } catch (JSONException e)
                        {
                           logger.warn("Could not de-serialize events", e);
                        }
                     }
                  });
               } finally
               {
                  lock.unlock();
               }
            }
         };
      }

      public Input<TransactionApplicationEvents, IOException> transactionsBefore(final long beforeTimestamp, final long maxTransactions)
      {
         return new Input<TransactionApplicationEvents, IOException>()
         {
            public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super TransactionApplicationEvents, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
            {
               // Lock datastore first
               lock.lock();
               try
               {
                  output.receiveFrom(new Sender<TransactionApplicationEvents, IOException>()
                  {
                     public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super TransactionApplicationEvents, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                     {
                        try
                        {
                           long count = 0;
                           Long startTime = beforeTimestamp - 1;
                           Collection<String> txsBeforeDate = store.headMap(startTime).values();

                           // Reverse the list - this could be done more easily in JDK1.6
                           LinkedList<String> values = new LinkedList<String>();
                           for (String json : txsBeforeDate)
                           {
                              values.addFirst(json);
                           }

                           for (String txJson : values)
                           {
                              JSONTokener tokener = new JSONTokener(txJson);
                              JSONObject json = (JSONObject) tokener.nextValue();
                              TransactionApplicationEvents tx = (TransactionApplicationEvents) transactionEventsType.fromJSON(json, module);

                              receiver.receive(tx);
                              count++;
                              if (count == maxTransactions)
                                 return;
                           }
                        } catch (JSONException e)
                        {
                           logger.warn("Could not de-serialize events", e);
                        }
                     }
                  });
               } finally
               {
                  lock.unlock();
               }
            }
         };
      }

      protected void rollback()
              throws IOException
      {
      }

      protected void commit()
              throws IOException
      {
      }

      @Override
      protected void storeEvents(TransactionApplicationEvents transactionDomain) throws IOException
      {
         String jsonString = transactionDomain.toString();
         store.put(transactionDomain.timestamp().get(), jsonString);
      }
   }
}