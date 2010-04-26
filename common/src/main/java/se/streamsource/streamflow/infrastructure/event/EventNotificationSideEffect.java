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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.sideeffect.SideEffectOf;

/**
 * Notify event listeners that an event was created by the factory
 */
public class EventNotificationSideEffect
      extends SideEffectOf<DomainEventFactory>
      implements DomainEventFactory
{
   @Service
   Iterable<EventListener> listeners;

   public DomainEvent createEvent( EntityComposite entity, String name, Object[] args )
   {
      DomainEvent event = result.createEvent( entity, name, args );

      for (EventListener listener : listeners)
      {
         listener.notifyEvent( event );
      }

      return null;
   }
}
