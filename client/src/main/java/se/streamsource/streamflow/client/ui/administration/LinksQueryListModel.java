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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;

import java.util.List;

public class LinksQueryListModel
      implements Refreshable
{
   private EventList<LinkValue> links = new BasicEventList<LinkValue>();
   private CommandQueryClient client;
   private String query;

   public LinksQueryListModel( @Uses CommandQueryClient client, @Uses String query )
   {
      this.client = client;
      this.query = query;
   }

   public EventList<LinkValue> getEventList()
   {
      return links;
   }

   public void refresh() throws OperationException
   {
      try
      {
         List<LinkValue> linkList = client.query( query, LinksValue.class ).links().get();
         EventListSynch.synchronize( linkList, links );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }
}
