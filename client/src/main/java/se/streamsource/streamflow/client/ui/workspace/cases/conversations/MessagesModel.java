/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.resource.conversation.*;

public class MessagesModel
   implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<MessageDTO> messages = new BasicEventList<MessageDTO>();

   public void refresh()
   {
      LinksValue messagesLinks = client.query( "index", LinksValue.class );
      EventListSynch.synchronize( messagesLinks.links().get(), messages );
   }

   public EventList<MessageDTO> messages()
   {
      return messages;
   }

   public void createMessage( String message )
   {
      ValueBuilder<StringValue> stringBuilder = vbf.newValueBuilder( StringValue.class );
      stringBuilder.prototype().string().set( message );
      client.postCommand( "createmessage", stringBuilder.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }
}