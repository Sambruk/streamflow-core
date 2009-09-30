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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.Organizations;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;

import java.util.Map;

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

    public Organizations organizations;
    public Map<String, Organization> organizationsCreated;

    @Given("the organizations")
    public Organizations givenOrganizations()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        organizations = uow.get(Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
        return organizations;
    }

    @When("a new organization named $name is created")
    public Organization createOrganization(String name) throws Exception
    {
        genericSteps.clearEvents();
        Organization organization = null;
        try
        {
            organization = organizations.createOrganization(name);
        } catch (Exception e)
        {
            genericSteps.setThrowable(e);
        }
        return organization;
    }


    @When("a new user named $newUser is created")
    public void createUser(String newUser) throws UnitOfWorkCompletionException
    {
        genericSteps.clearEvents();
        try
        {
            organizations.createUser(newUser, newUser);
        } catch(IllegalArgumentException e)
        {
            genericSteps.setThrowable(e);
        }
    }
}