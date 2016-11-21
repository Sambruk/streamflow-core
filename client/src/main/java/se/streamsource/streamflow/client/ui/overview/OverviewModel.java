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
package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.table.RowValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.client.ui.overview.OverviewResources.assignments_node;
import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */

public class OverviewModel
      implements Refreshable
{
   EventList<ContextItem> items = new BasicEventList<ContextItem>();

   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   public EventList<ContextItem> getItems()
   {
      return items;
   }

   public void refresh()
   {
      List<ContextItem> list = new ArrayList<ContextItem>( );

      ValueBuilder<TableQuery> builder = module.valueBuilderFactory().newValueBuilder(TableQuery.class);
      builder.prototype().tq().set("select *");

      TableValue projects = client.query("index", TableValue.class, builder.newInstance());
      for (RowValue project : projects.rows().get())
      {
         list.add( new ContextItem(projects.cell(project, "description").f().get(), text( assignments_node), Icons.assign.name(), (Integer) projects.cell(project, "assignments").v().get(), client.getClient( projects.cell(project, "href").f().get() ).getSubClient("assignments" )));
      }

      EventListSynch.synchronize( list, items );
   }

   public OverviewSummaryModel newOverviewSummaryModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(OverviewSummaryModel.class).use(client).newInstance();
   }
}
