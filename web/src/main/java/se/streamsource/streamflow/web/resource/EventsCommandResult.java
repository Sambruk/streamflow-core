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

package se.streamsource.streamflow.web.resource;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

/**
 * This filter will record all the Events from the call, and if any occured and the
 * result entity is EmptyRepresentation, will return these events as the result.
 */
public class EventsCommandResult
      implements CommandResult, TransactionVisitor
{
   @Structure
   protected ValueBuilderFactory vbf;

   private ThreadLocal<TransactionDomainEvents> transactions = new ThreadLocal<TransactionDomainEvents>();

   public Object getResult()
   {
      TransactionDomainEvents transactionDomain = transactions.get();
      if (transactionDomain == null)
      {
         ValueBuilder<TransactionDomainEvents> builder = vbf.newValueBuilder( TransactionDomainEvents.class );
         builder.prototype().timestamp().set( System.currentTimeMillis() );
         transactionDomain = builder.newInstance();
      } else
      {
         transactions.set( null ); // Clear for next request
      }

      return transactionDomain;
   }


   public boolean visit( TransactionDomainEvents transactionDomain )
   {
      transactions.set( transactionDomain );

      return true;
   }
}