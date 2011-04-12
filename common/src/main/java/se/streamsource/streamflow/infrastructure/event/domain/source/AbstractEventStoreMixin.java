/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.infrastructure.event.domain.source;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.service.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.property.*;
import org.qi4j.spi.structure.*;
import org.slf4j.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.time.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import static java.util.Collections.*;

/**
 * Base implementation for EventStores.
 */
public abstract class AbstractEventStoreMixin
      implements EventStore, EventStream, Activatable
{
   @Service
   Time time;

   @This
   protected Identity identity;

   protected Logger logger;
   protected ValueType domainEventType;
   protected ValueType transactionEventsType;

   protected Lock lock = new ReentrantLock();

   @Structure
   protected ModuleSPI module;

   @Structure
   private ValueBuilderFactory vbf;

   private ExecutorService transactionNotifier;

   final private List<TransactionListener> listeners = synchronizedList( new ArrayList<TransactionListener>() );

   private long lastTimestamp = 0;

   public void activate() throws IOException
   {
      logger = LoggerFactory.getLogger( identity.identity().get() );

      domainEventType = module.valueDescriptor( DomainEvent.class.getName() ).valueType();
      transactionEventsType = module.valueDescriptor( TransactionDomainEvents.class.getName() ).valueType();

      transactionNotifier = Executors.newSingleThreadExecutor();
   }

   public void passivate() throws Exception
   {
      transactionNotifier.shutdown();
      transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
   }

   // TransactionVisitor implementation
   // This is how transactions are put into the store


   public TransactionDomainEvents storeEvents( Iterable<DomainEvent> events )
         throws IOException
   {
      // Create new TransactionDomainEvents
      ValueBuilder<TransactionDomainEvents> builder = vbf.newValueBuilder( TransactionDomainEvents.class );
      Iterables.addAll( builder.prototype().events().get(), events );
      builder.prototype().timestamp().set( getCurrentTimestamp() );

      final TransactionDomainEvents transactionDomain = builder.newInstance();

      // Lock store so noone else can interrupt
      lock();
      try
      {
         storeEvents( transactionDomain );
      } finally
      {
         lock.unlock();
      }

      // Notify listeners
      transactionNotifier.submit( new Runnable()
      {
         public void run()
         {
            synchronized (listeners)
            {
               for (TransactionListener listener : listeners)
               {
                  try
                  {
                     listener.notifyTransactions( Collections.singleton( transactionDomain ));
                  } catch (Exception e)
                  {
                     logger.warn( "Could not notify event listener", e );
                  }
               }
            }
         }
      } );

      return transactionDomain;
   }

   // EventStream implementation
   public void registerListener( TransactionListener subscriber )
   {
      listeners.add( subscriber );
   }

   public void unregisterListener( TransactionListener subscriber )
   {
      listeners.remove( subscriber );
   }

   abstract protected void rollback()
         throws IOException;

   abstract protected void commit()
         throws IOException;

   abstract protected void storeEvents( TransactionDomainEvents transactionDomain )
         throws IOException;

   /**
    * Fix for this bug:
    * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
    */
   protected void lock()
   {
      while (true)
      {
         try
         {
            lock.tryLock( 1000, TimeUnit.MILLISECONDS );
            break;
         } catch (InterruptedException e)
         {
            // Try again
         }
      }
   }

   private synchronized long getCurrentTimestamp()
   {
      long timestamp = time.timeNow();
      if (timestamp <= lastTimestamp)
         timestamp = lastTimestamp + 1; // Increase by one to ensure uniqueness
      lastTimestamp = timestamp;
      return timestamp;
   }
}
