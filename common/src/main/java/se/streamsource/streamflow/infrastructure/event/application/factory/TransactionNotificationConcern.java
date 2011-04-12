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

package se.streamsource.streamflow.infrastructure.event.application.factory;

import org.qi4j.api.concern.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.unitofwork.*;
import org.slf4j.*;
import se.streamsource.streamflow.infrastructure.event.application.*;
import se.streamsource.streamflow.infrastructure.event.application.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.factory.*;

import java.io.*;

/**
 * Notify transaction listeners when a complete transaction of domain events is available.
 */
public class TransactionNotificationConcern
      extends ConcernOf<ApplicationEventFactory>
      implements ApplicationEventFactory
{
   @Service
   ApplicationEventStore eventStore;

   @Structure
   UnitOfWorkFactory uowf;

   Logger logger = LoggerFactory.getLogger( DomainEventFactory.class );

   public ApplicationEvent createEvent( String name, Object[] args )
   {
      final UnitOfWork unitOfWork = uowf.currentUnitOfWork();

      ApplicationEvent event = next.createEvent( name, args );

      // Add event to list in UoW
      UnitOfWorkApplicationEvents events = unitOfWork.metaInfo().get( UnitOfWorkApplicationEvents.class );
      if (events == null)
      {
         events = new UnitOfWorkApplicationEvents();
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
                  UnitOfWorkApplicationEvents events = unitOfWork.metaInfo().get( UnitOfWorkApplicationEvents.class );

                  try
                  {
                     eventStore.storeEvents( events.getEvents() );
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
}
