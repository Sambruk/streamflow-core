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

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.resource.overview.*;

import java.io.*;

public class OverviewSummaryModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   private BasicEventList<ProjectSummaryValue> projectOverviews = new BasicEventList<ProjectSummaryValue>();

   public Representation generateExcelProjectSummary() throws IOException, ResourceException
   {
      return client.queryRepresentation( "generateexcelprojectsummary", null );
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
