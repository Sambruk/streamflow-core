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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.ResponseHandler;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import javax.swing.SwingUtilities;

/**
 * JAVADOC
 */
public class ClientResponseHandler
   implements ResponseHandler
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   TransactionVisitor transactionVisitor;

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

               final TransactionEvents transactionEvents = vbf.newValueFromJSON( TransactionEvents.class, source );
               handleTransactionEvents( transactionEvents );
            }
         } catch (Exception e)
         {
            throw new RuntimeException( "Could not process events", e );
         }
      }
   }

   protected void handleTransactionEvents ( final TransactionEvents transactionEvents)
   {
      transactionVisitor.visit( transactionEvents );
/*
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            transactionVisitor.visit( transactionEvents );
         }
      });
*/
   }
}
