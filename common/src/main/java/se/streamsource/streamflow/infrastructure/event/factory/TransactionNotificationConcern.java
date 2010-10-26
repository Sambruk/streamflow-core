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

package se.streamsource.streamflow.infrastructure.event.factory;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.time.Time;

import java.io.IOException;

/**
 * Notify transaction listeners when a complete transaction of domain events is available.
 */
public class TransactionNotificationConcern
      extends ConcernOf<DomainEventFactory>
      implements DomainEventFactory
{
   @Service
   EventStore eventStore;

   @Service
   Iterable<TransactionVisitor> transactionVisitors;

   @Service
   Time time;

   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   Logger logger = LoggerFactory.getLogger( DomainEventFactory.class );

   private long lastTimestamp = 0;

   public DomainEvent createEvent( EntityComposite entity, String name, Object[] args )
   {
      final UnitOfWork unitOfWork = uowf.currentUnitOfWork();

      DomainEvent event = next.createEvent( entity, name, args );

      // Add event to list in UoW
      UnitOfWorkEvents events = unitOfWork.metaInfo().get( UnitOfWorkEvents.class );
      if (events == null)
      {
         events = new UnitOfWorkEvents();
         unitOfWork.metaInfo().set( events );

         unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
         {
            public void beforeCompletion() throws UnitOfWorkCompletionException
            {
            }

            public void afterCompletion( UnitOfWorkStatus status )
            {
               if (status.equals( UnitOfWorkStatus.COMPLETED ))
               {
                  UnitOfWorkEvents events = unitOfWork.metaInfo().get( UnitOfWorkEvents.class );

                  try
                  {
                     final TransactionEvents transaction = eventStore.storeEvents( events.getEvents() );

                     for (TransactionVisitor transactionVisitor : transactionVisitors)
                     {
                        try
                        {
                           transactionVisitor.visit( transaction );
                        } catch (Exception e)
                        {
                           logger.warn( "Could not deliver transaction", e );

                        }
                     }
                  } catch (IOException e)
                  {
                     logger.error( "Could not store events", e );
                     // How do we handle this? This is a major error!
                  }
               }
            }
         } );
      }

      events.add( event );

      return event;
   }

   protected synchronized long getCurrentTimestamp()
   {
      long timestamp = time.timeNow();
      if (timestamp <= lastTimestamp)
         timestamp = lastTimestamp + 1; // Increase by one to ensure uniqueness
      lastTimestamp = timestamp;
      return timestamp;
   }

}
