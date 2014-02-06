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
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import java.util.Observable;

import static org.qi4j.api.specification.Specifications.or;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class ConversationsModel
   extends Observable
   implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   TransactionList<ConversationDTO> conversations = new TransactionList<ConversationDTO>(new BasicEventList<ConversationDTO>( ));

   public void refresh()
   {
      ResourceValue resource = client.query();
      final LinksValue newConversations = (LinksValue) resource.index().get();
      EventListSynch.synchronize( newConversations.links().get(), conversations );

      setChanged();
      notifyObservers( resource );
   }

   public EventList<ConversationDTO> conversations()
   {
      return conversations;
   }

   public void createConversation( String topic )
   {
      ValueBuilder<StringValue> newTopic = module.valueBuilderFactory().newValue(StringValue.class).buildWith();
      newTopic.prototype().string().set( topic );
      Form form = new Form();
      form.set("topic", topic);
      client.postCommand( "create", form );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( or( onEntities( client.getReference().getParentRef().getLastSegment() ), onEntities( conversations ), withNames( "setUnread" )), transactions ))
         refresh();
   }

   public ConversationModel newConversationModel(LinkValue selectedValue)
   {
      return module.objectBuilderFactory().newObjectBuilder(ConversationModel.class).use(client.getClient(selectedValue)).newInstance();
   }
}
