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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.*;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.dci.value.link.TitledLinksValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import static org.qi4j.api.specification.Specifications.and;
import static org.qi4j.api.specification.Specifications.or;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.onEntities;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 */
public class TitledLinkValueListModel
   extends ResourceModel<LinksValue>
   implements Refreshable, TransactionListener
{
   protected EventList<TitledLinkValue> linkValues = new TransactionList<TitledLinkValue>(new BasicEventList<TitledLinkValue>());
   protected EventList<TitledLinkValue> sortedValues = new SeparatorList<TitledLinkValue>(linkValues, new TitledLinkGroupingComparator(" "), 1, 10000);

   public void refresh()
   {
      super.refresh();

      EventListSynch.synchronize( getIndex().links().get(), linkValues );
   }

   public EventList<TitledLinkValue> getList()
   {
      return sortedValues;
   }

   public EventList<TitledLinkValue> getUnsortedList()
   {
      return linkValues;
   }

   public void remove( LinkValue link)
   {
      client.getClient( link ).delete();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( or( onEntities( client.getReference().getParentRef().getLastSegment() ), onEntities( client.getReference().getLastSegment() ),
            and( Events.withNames( "changedDescription" ), onEntities( linkValues ) )), transactions ) )
         refresh();
   }

   protected void handleException( ResourceException e)
   {
      if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
      {
         throw new OperationException( ErrorResources.valueOf( e.getStatus().getDescription() ), e);
      } else
         throw e;
   }
}
