/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.infrastructure.event.application.source;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Output;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.time.Time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.synchronizedList;

/**
 * Base implementation for ApplicationEventStores.
 */
public abstract class AbstractApplicationEventStoreMixin
      implements ApplicationEventStore, ApplicationEventStream, Activatable
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

   private ExecutorService transactionNotifier;

   final private List<Output<TransactionApplicationEvents, ? extends Throwable>> listeners = synchronizedList( new ArrayList<Output<TransactionApplicationEvents, ? extends Throwable>>() );

   private long lastTimestamp = 0;

   public void activate() throws IOException
   {
      logger = LoggerFactory.getLogger( identity.identity().get() );

      domainEventType = module.valueDescriptor( ApplicationEvent.class.getName() ).valueType();
      transactionEventsType = module.valueDescriptor( TransactionApplicationEvents.class.getName() ).valueType();

      transactionNotifier = Executors.newSingleThreadExecutor();
   }

   public void passivate() throws Exception
   {
      transactionNotifier.shutdown();
      transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
   }

   // TransactionVisitor implementation
   // This is how transactions are put into the store
   public TransactionApplicationEvents storeEvents( Iterable<ApplicationEvent> events ) throws IOException
   {
      // Create new TransactionApplicationEvents
      ValueBuilder<TransactionApplicationEvents> builder = module.valueBuilderFactory().newValueBuilder(TransactionApplicationEvents.class);
      Iterables.addAll( builder.prototype().events().get(), events );
      builder.prototype().timestamp().set( getCurrentTimestamp() );

      final TransactionApplicationEvents transactionDomain = builder.newInstance();

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
               Input<TransactionApplicationEvents, RuntimeException> input = Inputs.iterable( Collections.singleton( transactionDomain ) );
               for (Output<TransactionApplicationEvents, ? extends Throwable> listener : listeners)
               {
                  try
                  {
                     input.transferTo( listener );
                  } catch (Throwable e)
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


   public void registerListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
   {
      listeners.add( listener );
   }


   public void unregisterListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
   {
      listeners.remove( listener );
   }

   abstract protected void rollback()
         throws IOException;

   abstract protected void commit()
         throws IOException;

   abstract protected void storeEvents( TransactionApplicationEvents transactionDomain )
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
