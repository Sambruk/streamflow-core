/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.application.shared.steps;

import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;

import static org.jbehave.Ensure.*;

public class OverviewQueriesSteps
      extends Steps
{
   @Uses
   OrganizationsSteps orgsSteps;

   @Structure
   UnitOfWorkFactory uowf;

   private ProjectSummaryListDTO summaryList;
   private ProjectSummaryDTO summary;

   @When("overview is requested")
   public void requestOverview() throws UnitOfWorkCompletionException
   {
      uowf.currentUnitOfWork().apply();
      summaryList = orgsSteps.givenUser.getProjectsSummary();
   }

   @Then("summaryList contains one project named $projectName")
   public void checkProjectCount( String projectName )
   {
      ensureThat( summaryList.projectOverviews().get().size(), CoreMatchers.equalTo( 1 ) );
      summary = summaryList.projectOverviews().get().get( 0 );
      ensureThat( summary.project().get(), CoreMatchers.equalTo( projectName ) );
   }

   @Then("overview contains $count $column tasks")
   public void checkResult( long count, String column )
   {
      if ("inbox".equals( column ))
      {
         ensureThat( summary.inboxCount().get(), CoreMatchers.equalTo( count ) );
      } else if ("assigned".equals( column ))
      {
         ensureThat( summary.assignedCount().get(), CoreMatchers.equalTo( count ) );
      }
   }
}
