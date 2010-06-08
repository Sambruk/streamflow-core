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

package se.streamsource.streamflow.infrastructure.event.source.helper;

import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventQuery;

/**
 * Takes a list of DomainEvents and filters them according to a given event specification.
 */
public class EventVisitorFilter
      implements EventVisitor
{
   private EventVisitor visitor;
   private EventSpecification eventSpecification;

   public EventVisitorFilter( EventVisitor visitor, String... name )
   {
      this( new EventQuery().withNames( name ), visitor );
   }

   public EventVisitorFilter( String entityId, EventVisitor visitor, String... name )
   {
      this( new EventQuery().onEntities( entityId ).withNames( name ), visitor );
   }

   public EventVisitorFilter( EventSpecification eventSpecification, EventVisitor visitor )
   {
      this.visitor = visitor;
      this.eventSpecification = eventSpecification;
   }

   public boolean visit( DomainEvent event )
   {
      if (eventSpecification.accept( event ))
         return visitor.visit( event );
      else
         return true;
   }
}