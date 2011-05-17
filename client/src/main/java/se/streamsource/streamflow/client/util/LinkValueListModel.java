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

package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import static org.qi4j.api.specification.Specifications.or;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.onEntities;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 */
public class LinkValueListModel
   extends ResourceModel<LinksValue>
   implements Refreshable, TransactionListener
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected Module module;

   protected EventList<LinkValue> linkValues = new TransactionList<LinkValue>(new BasicEventList<LinkValue>());
   protected EventList<LinkValue> sortedValues = new SortedList<LinkValue>(linkValues, new LinkComparator());

   public void refresh()
   {
      super.refresh();

      EventListSynch.synchronize( getIndex().links().get(), linkValues );
   }

   public EventList<LinkValue> getList()
   {
      return sortedValues;
   }

   public EventList<LinkValue> getUnsortedList()
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
            Specifications.and( Events.withNames( "changedDescription" ), onEntities( linkValues ))), transactions ))
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
