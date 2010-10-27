/*
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

package se.streamsource.streamflow.client.ui.workspace.context;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.data.Reference;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.ContextItem;

import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class WorkspaceContextModel2
      implements Refreshable
{
   EventList<ContextItem> items = new BasicEventList<ContextItem>();

   @Uses
   CommandQueryClient client;

   LinksValue caseCounts;

   public EventList<ContextItem> getItems()
   {
      return items;
   }

   public void refresh()
   {
      // Refresh case counts in the background
      new Task<LinksValue, Object>( Application.getInstance() )
      {
         @Override
         protected LinksValue doInBackground() throws Exception
         {
            return client.query( "casecounts", LinksValue.class );
         }

         @Override
         protected void succeeded( LinksValue linksValue )
         {
            caseCounts = linksValue;
            applyCounts();
         }
      }.execute();

      List<ContextItem> list = new ArrayList<ContextItem>();

      LinksValue projects = client.query( "index", LinksValue.class );
      for (LinkValue contextLink : projects.links().get())
      {
         if (contextLink.rel().get().equals("drafts"))
         {
            list.add( new ContextItem( "", text( drafts_node ), "draft", -1, client.getClient( contextLink ) ) );
         } else if (contextLink.rel().get().equals("inbox"))
         {
            list.add( new ContextItem( contextLink.text().get(), text( inboxes_node ), "inbox", -1, client.getClient( contextLink ) ) );
         } else if (contextLink.rel().get().equals("assignments"))
         {
            list.add( new ContextItem( contextLink.text().get(), text( assignments_node ), "assign", -1, client.getClient( contextLink ) ) );
         }
      }

      EventListSynch.synchronize( list, items );

      applyCounts();
   }

   protected void applyCounts()
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
                  ContextItem item = items.get( i );
                  Reference reference = item.getClient().getReference();
                  String ref = reference.getPath();
                  if (ref.endsWith( "user/drafts/" )) // This is a hack. Not sure why user and project references are different...
                     ref = "user/drafts";
                  else
                     ref = ref.substring( 0, ref.length() - 1 );
                  if (ref.endsWith( linkValue.id().get() ))
                  {
                     item.setCount( Long.valueOf( linkValue.text().get() ) );
                     items.set( i, item );
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
}
