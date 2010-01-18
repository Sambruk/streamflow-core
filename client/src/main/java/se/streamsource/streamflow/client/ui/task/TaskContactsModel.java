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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * List of contacts for a task
 */
public class TaskContactsModel
      implements Refreshable, EventListener, EventVisitor
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   TransactionList<ContactValue> eventList = new TransactionList<ContactValue>(new BasicEventList<ContactValue>( ));

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "addedContact", "deletedContact", "updatedContact" );

   public TaskContactsModel()
   {
   }

   List<ContactValue> contacts = Collections.emptyList();

   public void refresh()
   {
      try
      {
         TaskContactsDTO contactsDTO = (TaskContactsDTO) client.query( "contacts", TaskContactsDTO.class ).buildWith().prototype();
         EventListSynch.synchronize( contactsDTO.contacts().get(), eventList );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList<ContactValue> getEventList()
   {
      return eventList;
   }

   public CommandQueryClient getTaskContactsClientResource()
   {
      return client;
   }

   public void createContact()
   {
      try
      {
         client.postCommand( "add", vbf.newValue( ContactValue.class ) );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_create_contact, e );
      }
   }

   public void removeElement( int selectedIndex )
   {
      try
      {
         client.getSubClient( selectedIndex+"" ).deleteCommand();
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_remove_contact, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (client.getReference().getParentRef().getLastSegment().equals( event.entity().get() ))
      {
         Logger.getLogger( "workspace" ).info( "Refresh task contacts" );
         refresh();
      }

      return false;
   }
}