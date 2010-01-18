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
import org.qi4j.api.concern.Concerns;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import static se.streamsource.streamflow.domain.interaction.gtd.States.*;
import static se.streamsource.streamflow.domain.interaction.gtd.States.DROPPED;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.MethodConstraintsConcern;

/**
 * Status for a task. Possible transitions are:
 * Active -> Completed, Dropped, Done
 * Done -> Active, Dropped, Completed
 * Completed -> Active
 * Dropped -> Archived
 */
@Concerns(MethodConstraintsConcern.class)
@Mixins(Status.Mixin.class)
public interface Status
{
   @HasStatus({ACTIVE, DONE})
   void complete();

   @HasStatus(ACTIVE)
   void done();

   @HasStatus({ACTIVE, DONE})
   void drop();

   @HasStatus({COMPLETED, DROPPED})
   void reactivate();

   @HasStatus(DONE)
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
         changedStatus( DomainEvent.CREATE, COMPLETED );
      }

      public void drop()
      {
         changedStatus( DomainEvent.CREATE, DROPPED );
      }

      public void redo()
      {
         changedStatus( DomainEvent.CREATE, States.ACTIVE );
      }

      public void done()
      {
         changedStatus( DomainEvent.CREATE, DONE );
      }

      public void reactivate()
      {
         changedStatus( DomainEvent.CREATE, States.ACTIVE );
      }

      public boolean isStatus( States status )
      {
         return status().get().equals(status);
      }
   }

}
