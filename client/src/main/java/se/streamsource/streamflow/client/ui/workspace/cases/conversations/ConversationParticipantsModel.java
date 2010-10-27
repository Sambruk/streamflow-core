/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

public class ConversationParticipantsModel
   implements Refreshable
{
   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> participants = new BasicEventList<LinkValue>();

   public EventList<LinkValue> participants()
   {
      return participants;
   }

   public EventList<LinkValue> possibleParticipants()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query("possibleparticipants", LinksValue.class);
         list.addAll(listValue.links().get());

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e);
      }
   }

   public void addParticipant( LinkValue participant )
   {
      client.postLink( participant );
   }

   public void removeParticipant( LinkValue link )
   {
      client.getSubClient( link.id().get() ).delete();
   }

   public void refresh()
   {
      LinksValue participants = client.query( "index", LinksValue.class );
      EventListSynch.synchronize( participants.links().get(), this.participants );
   }
}