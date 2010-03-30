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

import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;

/**
 * JAVADOC
 */
public class AssignmentsSteps
      extends Steps
{
   @Uses
   OrganizationsSteps orgsSteps;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   GenericSteps genericSteps;

   public TaskEntity givenTask;

/*
   @When("an assigned task is created")
   public void createAssignedTask()
   {
      UserEntity user = orgsSteps.givenUser;
      givenTask = (TaskEntity) projectsSteps.givenProject.createAssignedTask( user );
   }

   @When("assigned task is completed")
   public void complete()
   {
      projectsSteps.givenProject.completeAssignedTask( givenTask );
   }


   @When("assigned task is dropped")
   public void drop()
   {
      projectsSteps.givenProject.dropAssignedTask( givenTask );
   }

   @When("assigned task is forwarded to user $name")
   public void forward( String name )
   {
      UserEntity user = orgsSteps.givenOrganizations().getUserByName( name );
      projectsSteps.givenProject.forwardAssignedTaskTo( givenTask, user );
   }


   @When("assigned task is delegated to user $name")
   public void delegate( String name )
   {
      UserEntity user = orgsSteps.givenOrganizations().getUserByName( name );
      projectsSteps.givenProject.delegateAssignedTaskTo( givenTask, user );
   }
*/
}