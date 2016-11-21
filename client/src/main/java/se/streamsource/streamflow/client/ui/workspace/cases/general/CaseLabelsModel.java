/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.general;

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
 * Model for the list of currently selected labels of a case
 */
public class CaseLabelsModel
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

   public EventList<LinkValue> getPossibleLabels()
   {
      return EventListSynch.synchronize(client.query( "possiblelabels",
            LinksValue.class ).links().get(), new BasicEventList<LinkValue>());
   }

   public void addLabel( LinkValue addLabel )
   {
      client.postLink( addLabel );
   }

   public void removeLabel( LinkValue removeLabel )
   {
      client.getClient( removeLabel ).delete();
   }

   public LinkValue getKnowledgeBaseLink(LinkValue selected)
   {
      return client.getClient(selected).query("knowledgebase", LinkValue.class);
   }
}
