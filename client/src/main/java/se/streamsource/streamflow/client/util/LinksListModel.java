/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;

import java.util.List;

public class LinksListModel
      implements Refreshable
{
   protected CommandQueryClient client;
   private String query;

   protected BasicEventList<LinkValue> eventList = new BasicEventList<LinkValue>();

   public LinksListModel(@Uses CommandQueryClient client, @Uses String query)
   {
      this.client = client;
      this.query = query;
   }

   public void refresh()
   {
      List<LinkValue> links = client.query(query, LinksValue.class).links().get();
      EventListSynch.synchronize( links, eventList );
   }

   public CommandQueryClient getClient()
   {
      return client;
   }

   public EventList<LinkValue> getEventList()
   {
      return eventList;
   }
}
