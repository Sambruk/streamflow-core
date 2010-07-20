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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.caze.CaseValue;

import java.util.List;

/**
 * Base class for all models that list cases
 */
public class CasesTableModel
      implements EventListener, EventVisitor, Refreshable
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected ValueBuilderFactory vbf;

   protected LinksValue cases;

   protected BasicEventList<CaseValue> eventList = new BasicEventList<CaseValue>();

   private EventVisitorFilter eventFilter;

   public CasesTableModel()
   {
      eventFilter = new EventVisitorFilter( this, "addedLabel", "removedLabel", "changedDescription", "changedCaseType", "changedStatus",
            "changedOwner","assignedTo", "deletedEntity");
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( final DomainEvent event )
   {
      CaseValue updatedCase = getCase( event );

      if (updatedCase != null)
      {
         int idx = eventList.indexOf( updatedCase );
         ValueBuilder<CaseValue> valueBuilder = updatedCase.<CaseValue>buildWith();
         updatedCase = valueBuilder.prototype();

         String eventName = event.name().get();
         if (eventName.equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updatedCase.text().set( newDesc );
               eventList.set( idx, valueBuilder.newInstance() );
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         } else if (eventName.equals( "removedLabel" ))
         {
            String id = EventParameters.getParameter( event, "param1" );
            List<LinkValue> labels = updatedCase.labels().get().links().get();
            for (LinkValue label : labels)
            {
               if (label.id().get().equals( id ))
               {
                  labels.remove( label );
                  break;
               }
            }
            eventList.set( idx, valueBuilder.newInstance() );
         } else if ("addedLabel,changedCaseType,changedOwner,assignedTo,deletedEntity".indexOf(eventName) != -1)
         {
            refresh();
         } else if (eventName.equals("changedStatus"))
         {
            CaseStates newStatus = CaseStates.valueOf( EventParameters.getParameter( event, "param1" ));
            updatedCase.status().set( newStatus );
            eventList.set( idx, valueBuilder.newInstance() );            
         }
      }
      return true;
   }

   public EventList<CaseValue> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         final LinksValue newRoot = client.query( "cases", LinksValue.class );
         boolean same = newRoot.equals( cases );
         if (!same)
         {
               EventListSynch.synchronize( newRoot.links().get(), eventList );
               cases = newRoot;
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   private CaseValue getCase( DomainEvent event )
   {
      if (cases == null)
         return null;

      for (int i = 0; i < eventList.size(); i++)
      {
         CaseValue caseValue = eventList.get( i );
         if (caseValue.id().get().equals( event.entity().get() ))
         {
            return caseValue;
         }
      }

      return null;
   }
}