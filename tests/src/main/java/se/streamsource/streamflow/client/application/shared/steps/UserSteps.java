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
import static org.jbehave.Ensure.ensureThat;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.client.application.shared.steps.setup.UserSetupSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.domain.user.WrongPasswordException;

/**
 * JAVADOC
 */
public class UserSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetupSteps;

    @Uses
    GenericSteps genericSteps;

    @When("user named $userName changes password from $oldPassword to $newPassword")
    public void changePassword(String userName, String oldPassword, String newPassword) throws UnitOfWorkCompletionException
    {
        UserEntity user = userSetupSteps.userMap.get(userName);
        ensureThat(user, CoreMatchers.notNullValue());

        try
        {
            user.changePassword(oldPassword, newPassword);
        } catch (WrongPasswordException e)
        {
            genericSteps.setThrowable(e);
        }
    }


    @When("user named $userName tries to login with password $password")
    public void login(String userName, String password) throws UnitOfWorkCompletionException
    {
        genericSteps.clearEvents();
        UserEntity user = userSetupSteps.userMap.get(userName);
        user.login(password);
    }


    @When("user named $userName enabled is set to $state")
    public void setEnabled(String userName, String state) throws UnitOfWorkCompletionException
    {
        genericSteps.clearEvents();
        UserEntity user = userSetupSteps.userMap.get(userName);
        user.changeEnabled(Boolean.parseBoolean(state));
    }

    @When("user named $userName resets password with $newPassword")
    public void resetPassword(String userName, String newPassword) throws UnitOfWorkCompletionException
    {
        UserEntity user = userSetupSteps.userMap.get(userName);
        ensureThat(user, CoreMatchers.notNullValue());

        user.resetPassword(newPassword);
    }

}