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

package se.streamsource.streamflow.client.infrastructure.events;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import java.lang.ref.Reference;
import java.lang.ref.*;
import java.util.*;

/**
 * JAVADOC
 */
@Mixins(ClientEventSourceService.Mixin.class)
public interface ClientEventSourceService
      extends EventStream, ResponseHandler, ServiceComposite
{
   class Mixin
         implements EventStream, ResponseHandler, Activatable
   {
      @Structure
      ValueBuilderFactory vbf;

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
      }

      private List<Reference<TransactionListener>> listeners = new ArrayList<Reference<TransactionListener>>();

      // EventSource implementation

      public void registerListener( TransactionListener listener )
      {
         listeners.add( new WeakReference<TransactionListener>( listener ) );
      }

      public void unregisterListener( TransactionListener listener )
      {
         Iterator<Reference<TransactionListener>> referenceIterator = listeners.iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionListener> eventSourceListenerReference = referenceIterator.next();
            TransactionListener lstnr = eventSourceListenerReference.get();
            if (lstnr == null || lstnr.equals( listener ))
            {
               referenceIterator.remove();
               return;
            }
         }
      }

      public void handleResponse( Response response ) throws ResourceException
      {
         if (response.getStatus().isSuccess() &&
               (response.getRequest().getMethod().equals( Method.POST ) ||
                     response.getRequest().getMethod().equals( Method.DELETE ) ||
                     response.getRequest().getMethod().equals( Method.PUT )))
         {
            try
            {
               Representation entity = response.getEntity();
               if (entity != null && !(entity instanceof EmptyRepresentation))
               {
                  String source = entity.getText();

                  final TransactionDomainEvents transactionDomainEvents = vbf.newValueFromJSON( TransactionDomainEvents.class, source );
                  notifyTransactionListeners( transactionDomainEvents );
               }
            } catch (Exception e)
            {
               throw new RuntimeException( "Could not process events", e );
            }
         }
      }

      private void notifyTransactionListeners( TransactionDomainEvents transactionDomain )
      {
         Iterator<Reference<TransactionListener>> referenceIterator = new ArrayList<Reference<TransactionListener>>(listeners).iterator();
         while (referenceIterator.hasNext())
         {
            Reference<TransactionListener> eventSourceListenerReference = referenceIterator.next();
            TransactionListener lstnr = eventSourceListenerReference.get();
            if (lstnr != null)
            {
               lstnr.notifyTransactions( Collections.singleton( transactionDomain ));
            }
         }
      }

   }
}
