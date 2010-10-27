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

package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Specifications;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Specifications.*;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 */
public class LinkValueListModel
   implements Refreshable, TransactionListener
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected ValueBuilderFactory vbf;

   protected EventList<LinkValue> linkValues = new BasicEventList<LinkValue>();

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
      return linkValues;
   }

   public void remove( LinkValue link)
   {
      client.getClient( link ).delete();
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
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
