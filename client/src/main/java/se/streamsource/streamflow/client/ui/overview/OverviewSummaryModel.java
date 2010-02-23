/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;

import java.io.IOException;
import java.io.InputStream;

public class OverviewSummaryModel
{
   @Uses
   CommandQueryClient client;

   private BasicEventList<ProjectSummaryDTO> projectOverviews = new BasicEventList<ProjectSummaryDTO>( );

   public InputStream generateExcelProjectSummary() throws IOException, ResourceException
   {
      return client.queryStream( "generateexcelprojectsummary", null );
   }

   public EventList<ProjectSummaryDTO> getProjectOverviews()
   {
      return projectOverviews;
   }

   public void refresh()
   {
      try
      {
         ProjectSummaryListDTO newResource = client.query( "projectsummary", ProjectSummaryListDTO.class );
         projectOverviews.clear();
         projectOverviews.addAll( newResource.projectOverviews().get() );
      } catch (Exception e)
      {
         throw new OperationException( OverviewResources.could_not_refresh, e );
      }
   }
}
