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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.TransactionList;
import org.apache.commons.collections.map.HashedMap;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * A model that represents a resource. Handles refreshing and basic observability. Exposes list of commands and queries
 * as EventLists.
 */
public abstract class ResourceModel<INDEXTYPE>
      extends Observable
      implements Refreshable
{
   @Structure
   ObjectBuilderFactory obf;

   @Uses
   protected CommandQueryClient client;

   protected ResourceValue resourceValue;

   private TransactionList<LinkValue> commands = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );
   private TransactionList<LinkValue> queries = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );
   private TransactionList<LinkValue> resources = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );

   private Map<String, Class> relationModelMap = new HashMap<String, Class>();

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

   public TransactionList<LinkValue> getResources()
   {
      return resources;
   }

   public INDEXTYPE getIndex()
   {
      return resourceValue == null ? null : (INDEXTYPE) resourceValue.index().get();
   }

   public Object newResourceModel(LinkValue resource)
      throws IllegalArgumentException
   {
      Class modelClass = relationModelMap.get(resource.rel().get());
      if (modelClass == null)
         throw new IllegalArgumentException("Unknown relation type:"+resource.rel().get());

      return obf.newObjectBuilder(modelClass).use(client.getClient(resource)).newInstance();
   }

   @Override
   public boolean equals(Object obj)
   {
      return obj instanceof ResourceModel && client.getReference().equals(((ResourceModel) obj).client.getReference());
   }

   @Override
   public String toString()
   {
      return client.getReference().toString();
   }

   protected void relationModelMapping(String relation, Class modelClass)
   {
      relationModelMap.put(relation, modelClass);
   }
}
