/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;

/**
 * Model for the list of currently selected forms of an access point
 */
public class FormLabelsModel
   extends ResourceModel<LinksValue>
{
   @Uses
   CommandQueryClient client;

   EventList<LinkValue> labels = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );

   public EventList<LinkValue> getLabels()
   {
      return labels;
   }

   public void refresh()
   {
      super.refresh();
      EventListSynch.synchronize( getIndex().links().get(), labels );
   }

   public EventList<LinkValue> getPossibleForms()
   {
      return EventListSynch.synchronize(client.query( "possibleformsforcasetype",
            LinksValue.class ).links().get(), new BasicEventList<LinkValue>());
   }

   public void addForm( LinkValue addForm )
   {
      client.postLink( addForm );
   }

   public void removeForm( LinkValue removeForm )
   {
      client.getClient( removeForm ).delete();
   }

}
