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
import org.qi4j.api.injection.scope.Uses;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.table.RowValue;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.io.IOException;

public class OverviewSummaryModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   private BasicEventList<RowValue> projectOverviews = new BasicEventList<RowValue>();

   public Representation generateExcelProjectSummary() throws IOException, ResourceException
   {
      return client.query( "generateexcelprojectsummary", Representation.class );
   }

   public EventList<RowValue> getProjectOverviews()
   {
      return projectOverviews;
   }

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "projectsummary", TableValue.class ).rows().get(), projectOverviews );
   }
}
