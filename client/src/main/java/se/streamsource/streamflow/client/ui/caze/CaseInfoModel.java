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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.caze.CaseValue;

import java.util.Observable;

/**
 * Model for the quick info about a case.
 */
public class CaseInfoModel extends Observable implements Refreshable,
      EventListener, EventVisitor

{
   EventVisitorFilter eventFilter;

   private CommandQueryClient client;

   CaseValue caseValue;

   public CaseInfoModel( @Uses CommandQueryClient client )
   {
      this.client = client;
      eventFilter = new EventVisitorFilter( client.getReference().getLastSegment(), this, "changedOwner", "changedCaseType", "changedDescription", "assignedTo", "unassigned", "changedStatus" );
   }

   public CaseValue getInfo()
   {
      if (caseValue == null)
         refresh();

      return caseValue;
   }

   public void refresh()
   {
      try
      {
         caseValue = client.query( "info", CaseValue.class );

         setChanged();
         notifyObservers();

      } catch (Exception e)
      {
         throw new OperationException( CaseResources.could_not_refresh, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (!event.usecase().get().equals( "createcase" ))
      {
         refresh();
      }
      return true;
   }

}