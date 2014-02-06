/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

public class MessagesModel
   extends ResourceModel<LinksValue>
   implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   BasicEventList<MessageDTO> messages = new BasicEventList<MessageDTO>();

   public void refresh()
   {

      super.refresh();
      EventListSynch.synchronize( getIndex().links().get(), messages );

   }

   public EventList<MessageDTO> messages()
   {
      return messages;
   }
   
   public void createMessageFromDraft()
   {
      client.postCommand( "createmessagefromdraft" );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }

   public MessageModel newMessageModel( String href )
   {
      return module.objectBuilderFactory().newObjectBuilder( MessageModel.class ).use( client.getSubClient( href ) ).newInstance();
   }

   public MessageDraftModel newMessageDraftModel()
   {
      return module.objectBuilderFactory().newObjectBuilder( MessageDraftModel.class ).use( client.getSubClient( "messagedraft" ) ).newInstance();
   }
}