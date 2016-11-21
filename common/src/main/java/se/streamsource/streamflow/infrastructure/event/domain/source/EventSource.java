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
package se.streamsource.streamflow.infrastructure.event.domain.source;

/**
 * An EventSource is a source of events. Events are grouped in the transactions in which they were created.
 */
public interface EventSource
{
   /**
    * Get list of event transactions after the given timestamp. If they are on the exact same timestamp, they will not be included.
    * <p/>
    * The method uses the visitor pattern, so a visitor is sent in which is given each transaction, one at a time.
    *
    * @param afterTimestamp timestamp of transactions
    * @param visitor for transactions
    */
   void transactionsAfter( long afterTimestamp, TransactionVisitor visitor );

   /**
    * Get list of event transactions before the given timestamp. If they are on the exact same timestamp, they will not be included.
    * <p/>
    * The method uses the visitor pattern, so a visitor is sent in which is given each transaction, one at a time.
    * <p/>
    * The transactions are sent to the visitor with the latest transaction first, i.e. walking backwards in the stream.
    *
    * @param beforeTimestamp timestamp of transactions
    * @param visitor for transactions
    */
   void transactionsBefore( long beforeTimestamp, TransactionVisitor visitor);
}
