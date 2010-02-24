/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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
import ca.odell.glazedlists.SortedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.domain.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.conversation.ConversationDetailDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.conversation.NewMessageCommand;
import se.streamsource.streamflow.resource.roles.StringDTO;

import java.awt.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class TaskConversationModel
   implements EventListener, Refreshable, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Uses
   TaskConversationParticipantsModel participantsModel;

   @Structure
   ValueBuilderFactory vbf;

   private ConversationDetailDTO conversationDetail;
   BasicEventList<LinkValue> messages = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> participants = new BasicEventList<LinkValue>();

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "messageCreated" );

   public void refresh()
   {
      try
      {
         conversationDetail = client.query( "index", ConversationDetailDTO.class );
         EventListSynch.synchronize( conversationDetail.messages().get().links().get(), messages );
         EventListSynch.synchronize( conversationDetail.participants().get().links().get(), participants );

         participantsModel.setParticipants( conversationDetail.participants().get() );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList messages()
   {
      return messages;
   }

   public EventList participants()
   {
      return participants;     
   }

   public String getDescription()
   {
      return conversationDetail.description().get();
   }

   public TaskConversationParticipantsModel getParticipantsModel()
   {
      return participantsModel;
   }

   public void addMessage( String message )
   {
      try
      {
         ValueBuilder<StringDTO> stringBuilder = vbf.newValueBuilder( StringDTO.class );
         stringBuilder.prototype().string().set( message );
         client.postCommand( "addmessage", stringBuilder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_message, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
       eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (client.getReference().getLastSegment().equals( event.entity().get() ))
      {
         Logger.getLogger( "workspace" ).info( "Refresh conversation" );
         refresh();
      }

      return false;
   }
}
