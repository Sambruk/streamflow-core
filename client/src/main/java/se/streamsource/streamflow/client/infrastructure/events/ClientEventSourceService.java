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
package se.streamsource.streamflow.client.infrastructure.events;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.ResponseHandler;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
      Module module;

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

                  final TransactionDomainEvents transactionDomainEvents = module.valueBuilderFactory().newValueFromJSON(TransactionDomainEvents.class, source);
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
