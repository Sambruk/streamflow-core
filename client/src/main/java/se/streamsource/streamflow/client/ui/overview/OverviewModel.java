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

package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.resource.overview.ProjectSummaryValue;

import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.client.ui.overview.OverviewResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */

public class OverviewModel
      implements Refreshable
{
   EventList<ContextItem> items = new BasicEventList<ContextItem>();

   @Uses
   CommandQueryClient client;

   public EventList<ContextItem> getItems()
   {
      return items;
   }

   public void refresh()
   {
      List<ContextItem> list = new ArrayList<ContextItem>( );

      LinksValue projects = client.query("index", LinksValue.class );
      for (LinkValue link : projects.links().get())
      {
         ProjectSummaryValue project = (ProjectSummaryValue) link;
         list.add( new ContextItem(project.text().get(), text( assignments_node), Icons.assign.name(), project.assignedCount().get(), client.getClient( project.href().get() ).getSubClient("assignments" )));
      }

      EventListSynch.synchronize( list, items );
   }
}
