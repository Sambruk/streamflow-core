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

package se.streamsource.streamflow.client;

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.util.*;

import java.util.*;

/**
 * A model that represents a resource. Handles refreshing and basic observability. Exposes list of commands and queries
 * as EventLists.
 */
public abstract class ResourceModel<INDEXTYPE>
      extends Observable
      implements Refreshable
{
   @Uses
   protected CommandQueryClient client;

   protected ResourceValue resourceValue;

   private TransactionList<LinkValue> commands = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );
   private TransactionList<LinkValue> queries = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );
   private TransactionList<LinkValue> resources = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );

   public void refresh()
   {
      resourceValue = client.queryResource();

      EventListSynch.synchronize( resourceValue.commands().get(), commands );
      EventListSynch.synchronize( resourceValue.queries().get(), queries );
      EventListSynch.synchronize( resourceValue.resources().get(), resources );

      setChanged();
      notifyObservers( resourceValue );
   }

   public ResourceValue getResourceValue()
   {
      return resourceValue;
   }

   public TransactionList<LinkValue> getCommands()
   {
      return commands;
   }

   public TransactionList<LinkValue> getQueries()
   {
      return queries;
   }

   public INDEXTYPE getIndex()
   {
      return resourceValue == null ? null : (INDEXTYPE) resourceValue.index().get();
   }
}
