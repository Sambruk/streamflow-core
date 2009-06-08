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

import static org.hamcrest.CoreMatchers.*;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.*;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

/**
 * JAVADOC
 */
public class LoginSteps
        extends Steps
{
    @Service
    Restlet client;

    @Service
    IndividualRepository individualRepository;

    private String result;

    public Account account;

    @Given("an account")
    public void givenAnAccount()
    {
        account = individualRepository.individual().newAccount();
        ValueBuilder<AccountSettingsValue> builder = account.settings().buildWith();
        builder.prototype().server().set("http://localhost:8040");
        builder.prototype().userName().set("");
        builder.prototype().password().set("");
        account.updateSettings(builder.newInstance());
    }

    @When("username is $username and password is $password")
    public void whenLoginWith(String username, String password)
    {
        ValueBuilder<AccountSettingsValue> builder = account.settings().buildWith();
        builder.prototype().userName().set(username);
        builder.prototype().password().set(password);
        account.updateSettings(builder.newInstance());

        try
        {
            account.server(client).version();
            result = "Ok";
        } catch (OperationException e)
        {
            result = e.getMessage();
        }
    }


    @When("users registers")
    public void whenUserRegisters() throws Exception
    {
        account.register(client);
    }

    @Then("login is $res")
    public void thenLoginResult(String res)
    {
        ensureThat(res, equalTo(result));
    }

    @Then("users $can register")
    public void thenUserCanRegister(final String can) throws Exception
    {
        if (can.equals("can"))
        {
            ensureThat(account.isRegistered());
        } else
        {
            ensureThat(can, equalTo("cannot"));
        }
    }

}
