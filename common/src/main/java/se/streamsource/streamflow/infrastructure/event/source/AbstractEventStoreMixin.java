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

package se.streamsource.streamflow.infrastructure.event.source;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Base implementation for EventStores.
 */
public abstract class AbstractEventStoreMixin
      implements EventStore, EventListener, Activatable
{
   @This
   protected Identity identity;

   protected Logger logger;
   protected ValueType domainEventType;
   protected ValueType transactionEventsType;

   protected ReentrantLock lock = new ReentrantLock();

   @Structure
   protected ModuleSPI module;

   @Structure
   private UnitOfWorkFactory uowf;

   @Structure
   private ValueBuilderFactory vbf;

   private Map<UnitOfWork, List<DomainEvent>> uows = new ConcurrentHashMap<UnitOfWork, List<DomainEvent>>();

   private long lastTimestamp = 0;

   public void activate() throws IOException
   {
      logger = Logger.getLogger( identity.identity().get() );

      domainEventType = module.valueDescriptor( DomainEvent.class.getName() ).valueType();
      transactionEventsType = module.valueDescriptor( TransactionEvents.class.getName() ).valueType();
   }

   public void passivate() throws Exception
   {
   }

   public void notifyEvent( DomainEvent event )
   {
      final UnitOfWork unitOfWork = uowf.currentUnitOfWork();
      List<DomainEvent> events = uows.get( unitOfWork );
      if (events == null)
      {
         final List<DomainEvent> eventList = new ArrayList<DomainEvent>();
         unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
         {
            public void beforeCompletion() throws UnitOfWorkCompletionException
            {
               if (eventList.size() > 0)
               {
                  try
                  {
                     // Lock store so noone else can interrupt
                     lock();

                     // Store all events from this UoW as one transaction
                     ValueBuilder<TransactionEvents> builder = vbf.newValueBuilder( TransactionEvents.class );
                     builder.prototype().timestamp().set( getCurrentTimestamp() );
                     builder.prototype().events().set( eventList );
                     TransactionEvents transaction = builder.newInstance();

                     storeEvents( transaction );
                  } catch (Exception e)
                  {
                     lock.unlock();
                     throw new UnitOfWorkCompletionException( e );
                  }
               }
            }

            public void afterCompletion( UnitOfWorkStatus status )
            {
               try
               {
                  if (status.equals( UnitOfWorkStatus.COMPLETED ))
                  {
                     if (eventList.size() > 0)
                     {
                        commit();
                     }
                  } else
                  {
                     rollback();
                  }
               } catch (IOException e)
               {
                  e.printStackTrace();
               }

               // Unlock store so that others can use it
               if (lock.isLocked())
                  lock.unlock();

               uows.remove( unitOfWork );
            }
         } );
         events = eventList;
         uows.put( unitOfWork, events );
      }
      events.add( event );
   }

   abstract protected void rollback()
         throws IOException;

   abstract protected void commit()
         throws IOException;

   abstract protected void storeEvents( TransactionEvents transaction )
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


   protected long getCurrentTimestamp()
   {
      long timestamp = System.currentTimeMillis();
      if (timestamp <= lastTimestamp)
         timestamp = lastTimestamp + 1; // Increase by one to ensure uniqueness
      lastTimestamp = timestamp;
      return timestamp;
   }

}
