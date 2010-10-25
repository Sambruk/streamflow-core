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

package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.resource.overview.ProjectSummaryValue;

import java.io.IOException;
import java.io.InputStream;

public class OverviewSummaryModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   private BasicEventList<ProjectSummaryValue> projectOverviews = new BasicEventList<ProjectSummaryValue>();

   public InputStream generateExcelProjectSummary() throws IOException, ResourceException
   {
      return client.queryStream( "generateexcelprojectsummary", null );
   }

   public EventList<ProjectSummaryValue> getProjectOverviews()
   {
      return projectOverviews;
   }

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "projectsummary", LinksValue.class ).links().get(), projectOverviews );
   }
}
