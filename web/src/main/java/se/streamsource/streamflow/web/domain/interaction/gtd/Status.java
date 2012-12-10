/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.CLOSED;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.DRAFT;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.ON_HOLD;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.MethodConstraintsConcern;

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
   }

   interface Events
   {
      void changedStatus( @Optional DomainEvent event, CaseStates status );
   }

   abstract class Mixin
         implements Status, Events
   {
      @This
      Data data;

      public void open()
      {
         changedStatus( null, OPEN );
      }

      public void close()
      {
         changedStatus( null, CLOSED );
      }

      public void onHold()
      {
         changedStatus( null, ON_HOLD );
      }

      public void reopen()
      {
         changedStatus( null, OPEN );
      }

      public void resume()
      {
         changedStatus( null, OPEN );
      }

      public boolean isStatus( CaseStates status )
      {
         return data.status().get().equals(status);
      }

      public void changedStatus(@Optional DomainEvent event, CaseStates status)
      {
         data.status().set(status);
      }
   }

}
