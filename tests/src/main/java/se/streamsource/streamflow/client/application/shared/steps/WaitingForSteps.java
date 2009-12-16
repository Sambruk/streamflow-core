/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.application.shared.steps;

import org.hamcrest.CoreMatchers;
import static org.jbehave.Ensure.*;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

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

   public TaskEntity givenTask;

   @Given("first waitingFor task")
   public void givenWaitingForTask() throws UnitOfWorkCompletionException
   {
      uowf.currentUnitOfWork().apply();
      TaskListDTO list = projectsSteps.givenProject.waitingForTasks( orgsSteps.givenUser );
      TaskDTO task = list.tasks().get().get( 0 );
      givenTask = uowf.currentUnitOfWork().get( TaskEntity.class, task.task().get().identity() );
   }

   @When("waitingFor task is completed")
   public void completeWaitingForTask()
   {
      projectsSteps.givenProject.completeWaitingForTask( givenTask, orgsSteps.givenUser );
   }

   @Then("task is completed")
   public void taskStatusEqualsCompleted()
   {
      ensureThat( givenTask.status().get(), CoreMatchers.equalTo( TaskStates.COMPLETED ) );
   }

//  @When("waitingFor task is finished")
//  public void completeFinishedTask()
//	{
//  	projectsSteps.givenProject.completeWaitingForTask(givenTask, assignee)
//  	projectsSteps.givenProject.completeFinishedTask( givenTask );
//	}

/*
    void rejectFinishedTask(Task task);

    void dropWaitingForTask(Task task, Assignee assignee);

    void markWaitingForAsRead(Task task);

    void markWaitingForAsUnread(Task task);

    void rejectTask(Task task);
*/

}