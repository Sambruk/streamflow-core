/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.infrastructure.event.source.helper;

import org.qi4j.api.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

/**
 * Helper that enables a service to easily track transactions. Upon startup
 * the tracker will get all the transactions from the store since the last
 * check, and delegate them to the given TransactionVisitor. It will also register itself
 * with the store so that it can get continuous updates.
 *
 * Then, as transactions come in from the store, they will be processed in real-time.
 * If a transaction is successfully handled the configuration of the service, which must
 * extend TransactionTrackerConfiguration, will update the marker for the last successfully handled transaction.
 *
 */
public class TransactionTracker
   implements TransactionVisitor
{
   private Configuration<? extends TransactionTrackerConfiguration> configuration;
   private TransactionVisitor visitor;
   private EventStore store;
   private boolean started = false;
   private boolean upToSpeed = false;
   private Logger logger;

   public TransactionTracker( EventStore store,
                        Configuration<? extends TransactionTrackerConfiguration> configuration,
                        TransactionVisitor visitor)
   {
      this.configuration = configuration;
      this.visitor = visitor;
      this.store = store;

      logger = LoggerFactory.getLogger( visitor.getClass() );
   }

   public void start()
   {
      store.registerListener( this );
      started = true;

      // Get events since last check
      upToSpeed = true; // Pretend that we are up to speed from now on
      store.transactionsAfter( configuration.configuration().lastEventDate().get(), this);
   }

   public void stop()
   {
      if (started)
      {
         started = false;
         store.unregisterListener( this );
      }
   }

   public synchronized boolean visit( TransactionEvents transaction )
   {
      if (started && configuration.configuration().enabled().get())
      {
         if (!upToSpeed)
         {
            // The tracker has not handled successfully all transactions before,
            // so it needs to get the backlog first

            upToSpeed = true; // Pretend that we are up to speed from now on

            // Get all transactions from last timestamp, including the one in this call
            store.transactionsAfter( configuration.configuration().lastEventDate().get(), this);
            return upToSpeed; // Hopefully we have everything by now
         }

         try
         {
            boolean result = visitor.visit( transaction );

            if (result)
            {
               // Events in this transaction were handled successfully so store new marker
               configuration.configuration().lastEventDate().set( transaction.timestamp().get() );
               configuration.save();
            }

            return result;

         } catch (Throwable e)
         {
            // Events could not be handled so don't update the marker
            logger.error( "Could not handle events", e );

            upToSpeed = false; // We could not handle all transactions, so next time, start from the timestamp

            return false;
         }
      } else
      {
         return false;
      }
   }
}
