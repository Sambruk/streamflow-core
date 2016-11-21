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
package se.streamsource.streamflow.infrastructure.event.application.source;

import org.qi4j.api.io.Output;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;

/**
 * Stream of event transactions. Registering with a stream will
 * allow the subscriber to get callbacks when new transactions
 * are available. The callbacks are done asynchronously.
 */
public interface ApplicationEventStream
{
   void registerListener( Output<TransactionApplicationEvents, ? extends Throwable> listener);

   void unregisterListener( Output<TransactionApplicationEvents, ? extends Throwable> listener);
}
