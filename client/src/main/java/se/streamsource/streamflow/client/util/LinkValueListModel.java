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

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.specification.*;
import org.qi4j.api.structure.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.application.error.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;

import static org.qi4j.api.specification.Specifications.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 */
public class LinkValueListModel
   implements Refreshable, TransactionListener
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected Module module;

   protected EventList<LinkValue> linkValues = new TransactionList<LinkValue>(new BasicEventList<LinkValue>());
   protected EventList<LinkValue> sortedValues = new SortedList<LinkValue>(linkValues, new LinkComparator());

   private final String refresh;

   public LinkValueListModel()
   {
      this("index");
   }

   public LinkValueListModel(String refresh)
   {
      this.refresh = refresh;
   }

   public void refresh()
   {
      EventListSynch.synchronize( client.query( refresh, LinksValue.class ).links().get(), linkValues );
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
