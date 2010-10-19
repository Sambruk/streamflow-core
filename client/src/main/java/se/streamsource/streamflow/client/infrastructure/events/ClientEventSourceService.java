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

package se.streamsource.streamflow.client.infrastructure.events;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ClientEventSourceService.Mixin.class)
public interface ClientEventSourceService
      extends EventSource, TransactionVisitor, ServiceComposite
{
   class Mixin
         implements EventSource, TransactionVisitor, Activatable
   {
      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
      }

      private List<Reference<TransactionVisitor>> listeners = new ArrayList<Reference<TransactionVisitor>>();

      // EventSource implementation

      public void registerListener( TransactionVisitor visitor )
      {
         listeners.add( new WeakReference<TransactionVisitor>( visitor ) );
      }

      public void unregisterListener( TransactionVisitor subscriber )
      {
         Iterator<Reference<TransactionVisitor>> referenceIterator = listeners.iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionVisitor> eventSourceListenerReference = referenceIterator.next();
            TransactionVisitor lstnr = eventSourceListenerReference.get();
            if (lstnr == null || lstnr.equals( subscriber ))
            {
               referenceIterator.remove();
               return;
            }
         }
      }

      // TransactionVisitor implementation

      public boolean visit( TransactionEvents transaction )
      {
         Iterator<Reference<TransactionVisitor>> referenceIterator = new ArrayList<Reference<TransactionVisitor>>(listeners).iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionVisitor> eventSourceListenerReference = referenceIterator.next();
            TransactionVisitor lstnr = eventSourceListenerReference.get();
            if (lstnr != null)
            {
               lstnr.visit( transaction );
            }
         }

         return true;
      }
   }
}
