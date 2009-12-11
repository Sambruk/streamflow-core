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

package se.streamsource.streamflow.infrastructure.event.source;

import se.streamsource.streamflow.infrastructure.event.TransactionEvents;

/**
 * JAVADOC
 */
public class ForEvents
        implements TransactionHandler
{
    private EventHandlerFilter filter;

    public ForEvents( EventSpecification specification, EventHandler handler )
    {
        filter = new EventHandlerFilter(specification, handler);
    }

   public boolean handleTransaction( TransactionEvents transaction )
   {
      new TransactionEventAdapter( filter ).handleTransaction( transaction );
      return false;
   }
}