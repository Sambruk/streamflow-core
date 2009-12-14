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
public abstract class OnEvents
      implements TransactionHandler, Runnable
{
   EventSpecification specification;

   public OnEvents( String... eventNames )
   {
      specification = new EventQuery().withNames( eventNames );
   }

   public boolean handleTransaction( TransactionEvents transaction )
   {
      EventMatcher handler = new EventMatcher( specification )
      {
         @Override
         public void run()
         {
            OnEvents.this.run();
         }
      };
      new TransactionEventAdapter( handler ).handleTransaction( transaction );

      if (handler.matches())
         handler.run();

      return false;
   }
}
