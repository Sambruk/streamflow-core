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

package se.streamsource.streamflow.client.model.shared;

import org.hamcrest.CoreMatchers;
import org.jbehave.Ensure;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.Individual;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.resource.users.UserClientResource;

import java.io.IOException;

/**
 * JAVADOC
 */
public class IndividualSteps
    extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    @Service
    IndividualRepository repo;

    @Service
    Restlet client;

    Individual individual;

    Account account;

    String version;

    @Given("individual")
    public void givenIndividual() throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        individual = repo.individual();
        uow.complete();
    }

    @When("new account")
    public void whenNewAccount() throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        individual = uow.get(individual);
        account = individual.newAccount();
        ValueBuilder<AccountSettingsValue> settings = account.accountSettings().buildWith();
        settings.prototype().name().set("Test server");
        settings.prototype().server().set("http://localhost:8040/streamflow/");
        account.updateSettings(settings.newInstance());
        uow.complete();
    }

    @When("user is $name and password is $password")
    public void whenUserPassword(String name, String password) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        account = uow.get(account);
        ValueBuilder<AccountSettingsValue> settings = account.accountSettings().buildWith();
        settings.prototype().userName().set(name);
        settings.prototype().password().set(password);
        account.updateSettings(settings.newInstance());
        uow.complete();
    }

    @When("get version")
    public void whenGetVersion() throws IOException, ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        account = uow.get(account);
        version = account.server(client).version();
        uow.discard();
    }

    @Then("version is $version")
    public void thenVersion(String ver)
    {
        Ensure.ensureThat(version, CoreMatchers.equalTo(ver));
    }

    public UserClientResource user() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            return uow.get(account).user(client);
        } finally
        {
            uow.discard();
        }
    }
}
