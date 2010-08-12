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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.util.Iterator;
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
            "changedOwner", "assignedTo", "unassigned", "deletedEntity", "updatedContact", "addedContact", "deletedContact",
            "createdConversation", "submittedForm", "createdAttachment", "removedAttachment" );
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
         ValueBuilder<CaseValue> valueBuilder = updatedCase.buildWith();
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
         } else if ("addedLabel,changedCaseType,changedOwner,assignedTo,unassigned,deletedEntity".indexOf( eventName ) != -1)
         {
            refresh();
         } else if (eventName.equals( "changedStatus" ))
         {
            CaseStates newStatus = CaseStates.valueOf( EventParameters.getParameter( event, "param1" ) );
            updatedCase.status().set( newStatus );
            eventList.set( idx, valueBuilder.newInstance() );
            // update in case the case has moved to another project
            if (CaseStates.OPEN.equals( newStatus ))
            {
               refresh();
            }
         } else if ("addedContact".equals( eventName ))
         {
            if (!updatedCase.hasContacts().get())
            {
               String param1 = EventParameters.getParameter( event, "param1" );

               if (!isContactTemplate( param1 ))
               {
                  updatedCase.hasContacts().set( true );
                  eventList.set( idx, valueBuilder.newInstance() );
               }
            }
         } else if ("updatedContact".equals( eventName ))
         {
            if (!updatedCase.hasContacts().get())
            {
               String param2 = EventParameters.getParameter( event, "param2" );

               if (!isContactTemplate( param2 ))
               {
                  updatedCase.hasContacts().set( true );
                  eventList.set( idx, valueBuilder.newInstance() );
               }
            }
         } else if (eventName.equals( "deletedContact" ))
         {
            refresh();
            // force list repaint
            EventListSynch.synchronize( cases.links().get(), eventList );
         } else if (eventName.equals( "createdConversation" ))
         {
            if (!updatedCase.hasConversations().get())
            {
               updatedCase.hasConversations().set( true );
               eventList.set( idx, valueBuilder.newInstance() );
            }
         } else if (eventName.equals( "submittedForm" ))
         {
            if (!updatedCase.hasSubmittedForms().get())
            {
               updatedCase.hasSubmittedForms().set( true );
               eventList.set( idx, valueBuilder.newInstance() );
            }
         } else if (eventName.equals( "createdAttachment" ))
         {
            if (!updatedCase.hasAttachments().get())
            {
               updatedCase.hasAttachments().set( true );
               eventList.set( idx, valueBuilder.newInstance() );
            }
         } else if (eventName.equals( "removedAttachment" ))
         {
            refresh();
            // force list repaint
            EventListSynch.synchronize( cases.links().get(), eventList );
         }
      }
      return true;
   }

   private boolean isContactTemplate( String eventParam )
   {
      boolean result = true;
      try
      {
         JSONObject contact = new JSONObject( eventParam );
         Iterator<String> iterator = contact.keys();
         while (iterator.hasNext())
         {
            String key = iterator.next();
            if (("name".equals( key ) && !"".equals( contact.getString( key ) ))
                  || ("company".equals( key ) && !"".equals( contact.getString( key ) ))
                  || ("contactId".equals( key ) && !"".equals( contact.getString( key ) ))
                  || ("note".equals( key ) && !"".equals( contact.getString( key ) ))
                  || ("picture".equals( key ) && !"".equals( contact.getString( key ) )))
            {
               result = false;
            } else if ("addresses".equals( key ) || "emailAddresses".equals( key ) || "phoneNumbers".equals( key ))
            {
               JSONArray list = contact.getJSONArray( key );
               if (list.length() != 0)
                  result = false;
            }

         }
      } catch (JSONException e)
      {
         result = false;
      }
      return result;
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

      for (CaseValue caseValue : eventList)
      {
         if (caseValue.id().get().equals( event.entity().get() ))
         {
            return caseValue;
         }
      }

      return null;
   }
}