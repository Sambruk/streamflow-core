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

import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
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
   UsersSteps usersSteps;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   GenericSteps genericSteps;

   public CaseEntity givenCase;

   @When("inbox task is created")
   public void createTask()
   {
      givenCase = (CaseEntity) projectsSteps.givenProject.createCase();
   }

   @When("inbox task is forwarded to user $name")
   public void forward( String name )
   {
      try
      {
         UserEntity user = usersSteps.givenUsers().getUserByName( name );
         givenCase.sendTo( user );
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
         givenCase.complete();
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   /*
  void receiveTask(Case task);


  void dropTask(Case task, Assignee assignee);

  void assignTo(Case task, Assignee assignee);

  void delegateTo(Case task, Delegatee delegatee, Delegator delegator);

   */
}