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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Generator for id sequences. Format is: yyyyMMdd-nnnn.
 */
@Mixins(IdGenerator.IdGeneratorMixin.class)
public interface IdGenerator
{
   void assignId( CompletableId completable );

   interface Data
   {
      @UseDefaults
      Property<Long> current();

      @Optional
      Property<Long> lastIdDate();

      void setCounter( DomainEvent event, long counter );

      void changedDate( DomainEvent create, long timeInMillis );
   }

   abstract class IdGeneratorMixin
         implements IdGenerator, Data
   {
      @This
      Data state;

      // Commands

      public void assignId( CompletableId completable )
      {
         // Check if we should reset the counter
         Calendar now = Calendar.getInstance();
         if (state.lastIdDate().get() != null)
         {
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTimeInMillis( state.lastIdDate().get() );

            // Day has changed - reset counter
            if (now.get( Calendar.DAY_OF_YEAR ) != lastDate.get( Calendar.DAY_OF_YEAR ))
            {
               state.setCounter( DomainEvent.CREATE, 0 );
            }
         }
         // Save current date
         state.changedDate( DomainEvent.CREATE, now.getTimeInMillis() );

         SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );

         long current = state.current().get();
         current++;
         setCounter( DomainEvent.CREATE, current );

         String date = format.format( now.getTime() );

         String caseId = date + "-" + current;

         completable.assignId( caseId );
      }

      // Events

      public void changedDate( DomainEvent create, long timeInMillis )
      {
         state.lastIdDate().set( timeInMillis );
      }

      public void setCounter( DomainEvent event, long counter )
      {
         state.current().set( counter );
      }
   }
}
