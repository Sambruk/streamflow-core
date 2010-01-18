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

import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Query that restricts what events to return. Uses fluent API style to build
 * up what criteria is used to accept an event.
 */
public class EventQuery
      implements EventSpecification
{
   private Date afterDate; // Only return events after this date
   private Date beforeDate; // Only return events before this date
   private List<String> names; // Only return events with these names
   private List<String> usecases; // Only return events with these usecases
   private List<String> entities; // Only return events on these entity
   private List<String> by; // Only return events caused by these users

   public EventQuery()
   {
   }

   public EventQuery afterDate( Date afterDate )
   {
      this.afterDate = afterDate;
      return this;
   }

   public EventQuery beforeDate( Date beforeDate )
   {
      this.beforeDate = beforeDate;
      return this;
   }

   public EventQuery withUsecases( String... name )
   {
      if (usecases == null)
         usecases = new ArrayList<String>();

      usecases.addAll( Arrays.asList( name ) );

      return this;
   }

   public EventQuery withNames( String... name )
   {
      if (names == null)
         names = new ArrayList<String>();

      names.addAll( Arrays.asList( name ) );

      return this;
   }

   public EventQuery onEntities( String... entities )
   {
      if (this.entities == null)
         this.entities = new ArrayList<String>();

      this.entities.addAll( Arrays.asList( entities ) );
      return this;
   }

   public EventQuery by( String... by )
   {
      if (this.by == null)
         this.by = new ArrayList<String>();

      this.by.addAll( Arrays.asList( by ) );
      return this;
   }

   public boolean accept( DomainEvent event )
   {
      // Check criteria
      if (afterDate != null && event.on().get().before( afterDate ))
         return false;

      if (beforeDate != null && event.on().get().after( beforeDate ))
         return false;

      if (usecases != null && !usecases.contains( event.usecase().get() ))
         return false;

      if (names != null && !names.contains( event.name().get() ))
         return false;

      if (entities != null && !entities.contains( event.entity().get() ))
         return false;

      if (by != null && !by.contains( event.by().get() ))
         return false;

      return true; // Event is accepted
   }
}
