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
import static org.jbehave.Ensure.ensureThat;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;

/**
 * JAVADOC
 */
public class WaitingForSteps
      extends Steps
{
   @Structure
   UnitOfWorkFactory uowf;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   @Uses
   GenericSteps genericSteps;

   public CaseEntity givenCase;

   @Given("first waitingFor task")
   public void givenWaitingForTask() throws UnitOfWorkCompletionException
   {
/*
      uowf.currentUnitOfWork().apply();
      TaskListDTO list = projectsSteps.givenProject.waitingFor( orgsSteps.givenUser );
      CaseValue task = list.tasks().get().get( 0 );
      givenCase = uowf.currentUnitOfWork().get( CaseEntity.class, task.task().get().identity() );
*/
   }

/*
   @When("waitingFor task is completed")
   public void completeWaitingForTask()
   {
      projectsSteps.givenProject.completeWaitingForTask( givenCase, orgsSteps.givenUser );
   }
*/

   @Then("task is completed")
   public void taskStatusEqualsCompleted()
   {
      ensureThat( givenCase.status().get(), CoreMatchers.equalTo( States.COMPLETED ) );
   }

//  @When("waitingFor task is finished")
//  public void completeFinishedTask()
//	{
//  	projectsSteps.givenProject.completeWaitingForTask(givenCase, assignee)
//  	projectsSteps.givenProject.completeFinishedTask( givenCase );
//	}

/*
    void rejectFinishedTask(Case task);

    void dropWaitingForTask(Case task, Assignee assignee);

    void markWaitingForAsRead(Case task);

    void markWaitingForAsUnread(Case task);

    void rejectTask(Case task);
*/

}