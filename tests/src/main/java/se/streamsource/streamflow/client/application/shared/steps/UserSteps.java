/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import static org.jbehave.Ensure.*;

/**
 * JAVADOC
 */
public class UserSteps
      extends Steps
{
   @Uses
   OrganizationsSteps organizationsSteps;

   @Uses
   GenericSteps genericSteps;

   @When("user changes password from $oldPassword to $newPassword")
   public void changePassword( String oldPassword, String newPassword ) throws UnitOfWorkCompletionException
   {
      UserEntity user = organizationsSteps.givenUser;
      ensureThat( user, CoreMatchers.notNullValue() );

      try
      {
         user.changePassword( oldPassword, newPassword );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }


   @When("user tries to login with password $password")
   public void login( String password ) throws UnitOfWorkCompletionException
   {
      organizationsSteps.givenUser.login( password );
   }


   @When("user enabled is set to $state")
   public void setEnabled( String state ) throws UnitOfWorkCompletionException
   {
      organizationsSteps.givenUser.changeEnabled( Boolean.parseBoolean( state ) );
   }

   @When("user resets password with $newPassword")
   public void resetPassword( String newPassword ) throws UnitOfWorkCompletionException
   {
      UserEntity user = organizationsSteps.givenUser;
      ensureThat( user, CoreMatchers.notNullValue() );

      user.resetPassword( newPassword );
   }

}