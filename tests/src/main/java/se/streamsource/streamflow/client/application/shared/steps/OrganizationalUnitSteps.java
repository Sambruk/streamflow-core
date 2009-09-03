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

import static org.hamcrest.CoreMatchers.equalTo;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;

/**
 * JAVADOC
 */
public class OrganizationalUnitSteps
        extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public OrganizationalUnit ou;

    private Exception thrownException;

    @Given("an organizational unit")
    public void givenOrganizationalUnit()
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        ou = uow.get(OrganizationEntity.class, "Organization");
    }

    @When("a new organizational unit named $name is created")
    public void createOrganizationUnit(String name) throws Exception
    {
        //UnitOfWork uow = uowf.newUnitOfWork();
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) ou;

        try
        {
            ouEntity.createOrganizationalUnit(name);
        } catch (Exception e)
        {
            thrownException = e;
        }

        //uow.complete();
    }


    @Then("organizational unit named $name is added")
    public void thenOrganizationUnitIsCreated(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) ou;

        boolean added = false;

        for (OrganizationalUnit unit : ouEntity.organizationalUnits())
        {
            OrganizationalUnitEntity entity = (OrganizationalUnitEntity) unit;
            if (entity.description().get().equals(name))
            {
                added = true;
            }
        }

        ensureThat(added);
    }

    @Then("organizational unit named $name is not added")
    public void thenOrganizationUnitIsNotCreated(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) ou;

        boolean added = false;

        for (OrganizationalUnit unit : ouEntity.organizationalUnits())
        {
            OrganizationalUnitEntity entity = (OrganizationalUnitEntity) unit;
            if (entity.description().get().equals(name))
            {
                added = true;
            }
        }

        ensureThat(!added);
    }


    @Then("$exceptionName is thrown")
    public void exceptionIsThrown(String exceptionName)
    {
        ensureThat(thrownException.getClass().getSimpleName(), equalTo(exceptionName));
    }
}