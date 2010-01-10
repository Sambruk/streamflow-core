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

import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;

/**
 * JAVADOC
 */
public class InboxSteps
      extends Steps
{
   @Uses
   OrganizationsSteps orgsSteps;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   GenericSteps genericSteps;

   public TaskEntity givenTask;

   @When("inbox task is created")
   public void createTask()
   {
      givenTask = (TaskEntity) projectsSteps.givenProject.createTask();
   }

   @When("inbox task is forwarded to user $name")
   public void forward( String name )
   {
      try
      {
         UserEntity user = orgsSteps.givenOrganizations().getUserByName( name );
         givenTask.sendTo( user );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   @When("task is completed")
   public void completeTask()
   {
      try
      {
         givenTask.complete();
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   /*
  void receiveTask(Task task);


  void dropTask(Task task, Assignee assignee);

  void assignTo(Task task, Assignee assignee);

  void delegateTo(Task task, Delegatee delegatee, Delegator delegator);

   */
}