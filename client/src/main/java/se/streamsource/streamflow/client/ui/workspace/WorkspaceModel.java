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
package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.data.Reference;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.workspace.cases.CasesModel;
import se.streamsource.streamflow.client.ui.workspace.search.PerspectivesModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.util.EventListSynch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * TODO
 */
public class WorkspaceModel
      extends ResourceModel
{
   @Structure
   Module module;

   EventList<ContextItem> items = new BasicEventList<ContextItem>();

   LinksValue caseCounts;

   public EventList<ContextItem> getItems()
   {
      return items;
   }

   public synchronized void refresh()
   {
      super.refresh();

      List<ContextItem> list = new ArrayList<ContextItem>();

      LinksValue projects = (LinksValue) getResourceValue().index().get();

      for (LinkValue contextLink : projects.links().get())
      {
         if (contextLink.rel().get().equals("drafts"))
         {
            list.add(new ContextItem("", text(drafts_node), "draft", -1, client.getClient(contextLink)));
         } else if (contextLink.rel().get().equals("search"))
         {
            list.add(new ContextItem("", text(search_node), "search", -1, client.getClient(contextLink)));
         } else if (contextLink.rel().get().equals("perspective"))
         {
            list.add(new ContextItem("", contextLink.text().get(), contextLink.rel().get(), -1, client.getClient(contextLink)));
         } else if (contextLink.rel().get().equals("inbox"))
         {
            list.add(new ContextItem(contextLink.text().get(), text(inboxes_node), "inbox", -1, client.getClient(contextLink)));
         } else if (contextLink.rel().get().equals("assignments"))
         {
            list.add(new ContextItem(contextLink.text().get(), text(assignments_node), "assign", -1, client.getClient(contextLink)));
         }
      }

      EventListSynch.synchronize(Collections.<ContextItem>emptyList(), items);
      EventListSynch.synchronize(list, items);

      applyCounts();
   }

   public synchronized void refreshCounts()
   {
      caseCounts = client.query("casecounts", LinksValue.class);
      applyCounts();
   }

   protected synchronized void applyCounts()
   {
      if (caseCounts != null)
      {
         for (LinkValue linkValue : caseCounts.links().get())
         {
            items.getReadWriteLock().writeLock().lock();
            try
            {
               for (int i = 0; i < items.size(); i++)
               {
                  ContextItem item = items.get(i);
                  Reference reference = item.getClient().getReference();
                  String ref = reference.getPath();
                  if (ref.endsWith("user/drafts/")) // This is a hack. Not sure why user and project references are different...
                     ref = "user/drafts";
                  else
                     ref = ref.substring(0, ref.length() - 1);
                  if (ref.endsWith(linkValue.id().get()))
                  {
                     item.setCount(Long.valueOf(linkValue.text().get()));
                     items.set(i, item);
                     break;
                  }
               }
            } finally
            {
               items.getReadWriteLock().writeLock().unlock();
            }
         }
      }
   }

   public CasesModel newCasesModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CasesModel.class).use(client.getSubClient("cases")).newInstance();
   }

   public SearchResultTableModel newSearchModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(SearchResultTableModel.class).use( client.getSubClient("search") ).newInstance();
   }

   public PerspectivesModel newPerspectivesModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(PerspectivesModel.class).use(client.getSubClient("perspectives")).newInstance();
   }

   public CasesTableModel newCasesTableModel(CommandQueryClient client)
   {
      return module.objectBuilderFactory().newObjectBuilder(CasesTableModel.class).use(client).newInstance();
   }
}
