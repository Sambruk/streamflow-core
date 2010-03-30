/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.task.conversations;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.task.TaskResources;

public class MessagesModel
   implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   private LinksValue messagesLinks;
   BasicEventList<LinkValue> messages = new BasicEventList<LinkValue>();

   public void refresh()
   {
      try
      {
         messagesLinks = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( messagesLinks.links().get(), messages );

      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList messages()
   {
      return messages;
   }

   public void addMessage( String message )
   {
      try
      {
         ValueBuilder<StringValue> stringBuilder = vbf.newValueBuilder( StringValue.class );
         stringBuilder.prototype().string().set( message );
         client.postCommand( "addmessage", stringBuilder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_message, e );
      }
   }
}