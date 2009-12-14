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

package se.streamsource.streamflow.client.infrastructure.events;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionCollector;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ClientEventSourceService.Mixin.class)
public interface ClientEventSourceService
      extends EventSource, TransactionHandler, ServiceComposite
{
   class Mixin
         implements EventSource, TransactionHandler, Activatable
   {
      Date after = new Date();

      public Reader reader;
      public Iterable<TransactionEvents> events;
      public TransactionCollector transactionCollector;

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
      }

      private List<Reference<TransactionHandler>> listeners = new ArrayList<Reference<TransactionHandler>>();

      // EventSource implementation

      public void registerListener( TransactionHandler handler )
      {
         listeners.add( new WeakReference<TransactionHandler>( handler ) );
      }

      public void unregisterListener( TransactionHandler subscriber )
      {
         Iterator<Reference<TransactionHandler>> referenceIterator = listeners.iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionHandler> eventSourceListenerReference = referenceIterator.next();
            TransactionHandler lstnr = eventSourceListenerReference.get();
            if (lstnr == null || lstnr.equals( subscriber ))
            {
               referenceIterator.remove();
               return;
            }
         }
      }

      // TransactionHandler implementation

      public boolean handleTransaction( TransactionEvents transaction )
      {
         Iterator<Reference<TransactionHandler>> referenceIterator = listeners.iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionHandler> eventSourceListenerReference = referenceIterator.next();
            TransactionHandler lstnr = eventSourceListenerReference.get();
            if (lstnr == null)
            {
               referenceIterator.remove();
            } else
            {
               lstnr.handleTransaction( transaction );
            }
         }

         return true;
      }
   }
}
