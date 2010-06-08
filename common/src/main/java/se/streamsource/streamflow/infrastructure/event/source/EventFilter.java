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

package se.streamsource.streamflow.infrastructure.event.source;

import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.AllEventsSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of TransactionEvents and filters them according to a given event specification.
 */
public class EventFilter
{
   public static final EventFilter ALL_EVENTS = new EventFilter( AllEventsSpecification.INSTANCE );

   private EventSpecification eventSpecification;
   private long lastTimestamp;

   public EventFilter()
   {
      this( new AllEventsSpecification() );
   }

   public EventFilter( EventSpecification eventSpecification )
   {
      this.eventSpecification = eventSpecification;
   }

   public Iterable<DomainEvent> events( Iterable<TransactionEvents> transactions )
   {
      List<DomainEvent> events = new ArrayList<DomainEvent>();
      for (TransactionEvents transaction : transactions)
      {
         for (DomainEvent domainEvent : transaction.events().get())
         {
            if (eventSpecification.accept( domainEvent ))
            {
               events.add( domainEvent );
            }
         }
         lastTimestamp = transaction.timestamp().get();
      }

      return events;
   }

   public Iterable<TransactionEvents> transactions( Iterable<TransactionEvents> transactions )
   {
      List<TransactionEvents> matchedTransactions = new ArrayList<TransactionEvents>();
      for (TransactionEvents transaction : transactions)
      {
         for (DomainEvent domainEvent : transaction.events().get())
         {
            if (eventSpecification.accept( domainEvent ))
            {
               // Found an event that matches - add tx and continue with next
               matchedTransactions.add( transaction );
               break;
            }
         }
         lastTimestamp = transaction.timestamp().get();
      }

      return matchedTransactions;
   }

   public boolean matchesAny( Iterable<TransactionEvents> transactions )
   {
      boolean matches = false;
      for (TransactionEvents transaction : transactions)
      {
         for (DomainEvent domainEvent : transaction.events().get())
         {
            if (eventSpecification.accept( domainEvent ))
            {
               matches = true;
            }
         }
         lastTimestamp = transaction.timestamp().get();
      }

      return matches;
   }

   /**
    * Timestamp of the last evalutated transaction. This can be used as input
    * to the next call to {@link EventStore#events(java.util.Date, int)}.
    *
    * @return
    */
   public long lastTimestamp()
   {
      return lastTimestamp;
   }
}
