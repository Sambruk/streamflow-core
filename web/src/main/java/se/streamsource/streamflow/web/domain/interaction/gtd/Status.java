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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.MethodConstraintsConcern;

import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.*;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.OPEN;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.ON_HOLD;

/**
 * Status for a case. Possible transitions are:
 * Draft -> Open
 * Open -> Closed, On_hold
 * Closed -> Open
 * On_hold -> Open
 */
@Concerns(MethodConstraintsConcern.class)
@Mixins(Status.Mixin.class)
public interface Status
{
   @RequiresStatus({DRAFT})
   void open();

   @RequiresStatus({OPEN})
   void close();

   @RequiresStatus({OPEN})
   void onHold();

   @RequiresStatus({CLOSED})
   void reopen();

   @RequiresStatus({ON_HOLD})
   void resume();

   boolean isStatus( CaseStates status );

   interface Data
   {
      @UseDefaults
      Property<CaseStates> status();

      void changedStatus( DomainEvent event, CaseStates status );
   }

   abstract class Mixin
         implements Status, Data
   {

      public void open()
      {
         changedStatus( DomainEvent.CREATE, OPEN );
      }

      public void close()
      {
         changedStatus( DomainEvent.CREATE, CLOSED );
      }

      public void onHold()
      {
         changedStatus( DomainEvent.CREATE, ON_HOLD );
      }

      public void reopen()
      {
         changedStatus( DomainEvent.CREATE, OPEN );
      }

      public void resume()
      {
         changedStatus( DomainEvent.CREATE, OPEN );
      }

      public boolean isStatus( CaseStates status )
      {
         return status().get().equals(status);
      }
   }

}
