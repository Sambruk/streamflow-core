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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Status for a task. Possible transitions are:
 * Active -> Completed, Dropped, Done
 * Done -> Active, Dropped, Completed
 * Completed -> Archived
 * Dropped -> Archived
 */
@Mixins(Status.Mixin.class)
public interface Status
{
   void complete();

   void done();

   void activate();

   void drop();

   void redo();

   boolean isStatus( States status );

   interface Data
   {
      @UseDefaults
      Property<States> status();

      void changedStatus( DomainEvent event, States status );
   }

   abstract class Mixin
         implements Status, Data
   {

      public void complete()
      {
         if (status().get().equals( States.ACTIVE ) || status().get().equals( States.DONE ))
         {
            changedStatus( DomainEvent.CREATE, States.COMPLETED );
         }
      }

      public void drop()
      {
         if (status().get().equals( States.ACTIVE ) || status().get().equals( States.DONE ))
         {
            changedStatus( DomainEvent.CREATE, States.DROPPED );
         }
      }

      public void redo()
      {
         if (status().get().equals( States.DONE))
         {
            changedStatus( DomainEvent.CREATE, States.ACTIVE );
         }
      }

      public void done()
      {
         if (status().get().equals( States.ACTIVE ))
         {
            changedStatus( DomainEvent.CREATE, States.DONE );
         }
      }

      public void activate()
      {
         if (status().get().equals( States.DONE ) || status().get().equals( States.COMPLETED ))
         {
            changedStatus( DomainEvent.CREATE, States.ACTIVE );
         }
      }

      public boolean isStatus( States status )
      {
         return status().get().equals(status);
      }
   }

}
