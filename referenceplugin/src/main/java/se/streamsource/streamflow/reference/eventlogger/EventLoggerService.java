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
package se.streamsource.streamflow.reference.eventlogger;

import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;

/**
 * JAVADOC
 */
public class EventLoggerService
   implements TransactionVisitor
{
   public EventStream stream;

   public boolean visit( TransactionDomainEvents transactionDomain )
   {
      for (DomainEvent domainEvent : transactionDomain.events().get())
      {
         LoggerFactory.getLogger( "events" ).info( domainEvent.name().get() );
      }

      return true;
   }
}
