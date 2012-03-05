/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.util;

import java.util.Collection;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;

/**
 * Synchronize an EventList with a collection. This is used for getting updates
 * from the server and showing them in the UI.
 * In conjunction with a SortedList <code>EventListSynch.synchronize()</code> may only be called on the
 * underlying BasicEventList!!
 */
public class EventListSynch
{
   public static <T, P extends T> EventList<P> synchronize( Collection<T> list, EventList<P> eventList )
   {
      eventList.getReadWriteLock().writeLock().lock();
      try
      {
         if (eventList instanceof TransactionList)
            ((TransactionList) eventList).beginEvent();

         if (list.size() == eventList.size())
         {
            // Same size
            int idx = 0;
            for (Object item : list)
            {
               if (!item.equals( eventList.get(idx )))
                  eventList.set( idx, (P) item );
               idx++;
            }

         } else if (list.size() < eventList.size())
         {
            eventList.clear();
            eventList.addAll( (Collection<? extends P>) list );
/*

            // New size is less than current
            int idx = 0;
            for (Object item : list)
            {
               if (!item.equals( eventList.get(idx )))
                  eventList.set( idx, (P) item );
               idx++;
            }

            // Remove remaining
            idx = list.size();
            while (eventList.size() > list.size())
               eventList.remove( idx );
*/
         } else
         {
            // New size is more than current
            int idx = 0;
            for (Object item : list)
            {
               if (idx < eventList.size())
               {
                  if (!item.equals( eventList.get(idx )))
                     eventList.set( idx, (P) item );
               } else
                  eventList.add( (P) item );

               idx++;
            }
         }

         if (eventList instanceof TransactionList)
            ((TransactionList) eventList).commitEvent();
      } finally
      {
         eventList.getReadWriteLock().writeLock().unlock();
      }

      return eventList;
   }
}
