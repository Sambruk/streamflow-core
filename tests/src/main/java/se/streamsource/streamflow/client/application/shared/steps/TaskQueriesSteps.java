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
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

import static org.jbehave.Ensure.*;

/**
 * JAVADOC
 */
public class TaskQueriesSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationsSteps organizationsSteps;

   @Uses
   InboxSteps inboxSteps;
   private ProjectEntity projectEntity;
   private TaskEntity task;

   @Given("project task")
   public void givenProjectTask()
   {
      inboxSteps.createTask();
      task = inboxSteps.givenTask;
   }

   @Given("user task")
   public void givenUserTask()
   {
      ensureThat( organizationsSteps.givenUser, CoreMatchers.notNullValue() );
      task = (TaskEntity) organizationsSteps.givenUser.createTask();
   }

   @When("querying for the owning project")
   public void owningProject()
   {
      projectEntity = task.ownerProject();
   }

   @Then("$projectName is returned")
   public void projectName( String projectName )
   {
      if (projectName.equals( "null" ))
      {
         ensureThat( projectEntity, CoreMatchers.nullValue() );
      } else
      {
         ensureThat( projectEntity.description().get(), CoreMatchers.equalTo( projectName ) );
      }
   }

}