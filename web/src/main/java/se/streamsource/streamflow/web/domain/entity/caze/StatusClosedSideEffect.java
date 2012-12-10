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
package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Closed;

/**
 * When a case is closed, then update the Closed data.
 */
public abstract class StatusClosedSideEffect
   extends SideEffectOf<Status.Events>
   implements Status.Events
{
   @This
   Closed closed;

   @Structure
   Module module;

   public void changedStatus( DomainEvent event, CaseStates status )
   {
      if (status.equals( CaseStates.CLOSED ))
      {
         closed.closedOn().set( event.on().get() );
         closed.closedBy().set( module.unitOfWorkFactory().currentUnitOfWork().get( Assignee.class, event.by().get() ));
      }
   }
}
