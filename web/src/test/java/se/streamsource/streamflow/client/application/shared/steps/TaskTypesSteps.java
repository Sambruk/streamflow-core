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

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypeEntity;

/**
 * JAVADOC
 */
public class TaskTypesSteps
      extends Steps
{
   @Uses
   OrganizationsSteps orgSteps;

   public TaskTypeEntity givenTaskType;


   @Given("tasktype named $tasktype")
   public void givenTaskType( String name )
   {
      givenTaskType = (TaskTypeEntity) orgSteps.givenOrganization.getTaskTypeByName( name );
   }

   @When("a tasktype named $name is created")
   public void createTaskType( String name )
   {
      givenTaskType = (TaskTypeEntity) orgSteps.givenOrganization.createTaskType( name );
   }

   @When("tasktype is removed")
   public void removeProject()
   {
      orgSteps.givenOrganization.removeTaskType( givenTaskType );
   }
}