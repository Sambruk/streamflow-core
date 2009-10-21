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

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.constraint.ConstraintViolationException;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class OrganizationsSteps
        extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Uses
    GenericSteps genericSteps;

    public OrganizationsEntity organizations;

    public OrganizationEntity givenOrganization;
    public UserEntity givenUser;

    @Given("organization named $org")
    public void givenOrganization(String name)
    {
        givenOrganization = organizations.getOrganizationByName( name );
    }

    @Given("user named $user")
    public void givenUser(String name)
    {
        givenUser = organizations.getUserByName( name );
    }

    @Given("the organizations")
    public OrganizationsEntity givenOrganizations()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        organizations = uow.get(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);
        return organizations;
    }

    @When("a new organization named $name is created")
    public void createOrganization(String name) throws Exception
    {
        try
        {
            givenOrganization = organizations.createOrganization(name);
        } catch (Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }


    @When("a new user named $newUser is created")
    public void createUser(String newUser) throws UnitOfWorkCompletionException
    {
        try
        {
            givenUser = organizations.createUser(newUser, newUser);
        } catch(IllegalArgumentException e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("a new user named $first $second is created")
    public void createUser(String first, String second) throws UnitOfWorkCompletionException
    {
        try
        {
            organizations.createUser(first + " " + second, "pwd");
        } catch(ConstraintViolationException e)
        {
            genericSteps.setThrowable(e);
        }
    }
}