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

import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;

/**
 * JAVADOC
 */
public class OrganizationalUnitSteps
        extends Steps
{
    @Uses
    UserSteps user;

    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    IndividualRepository individualRepository;

    public OrganizationalUnit ou;

/*
    @Given("an organizational unit")
    public void givenOrganizationalUnit()
    {
        navigator.individual().visitShared(new OrganizationalUnitVisitor()
        {
            public boolean visitOrganizationalUnit(OrganizationalUnit unit)
            {
                ou = unit;
                return false;
            }
        });

    }

    @When("groups named $name is created")
    public void createGroup(String name) throws Exception
    {
        ValueBuilder<NewGroupCommand> newGroupBuilder = vbf.newValueBuilder(NewGroupCommand.class);
        newGroupBuilder.prototype().context().set(ou);
        newGroupBuilder.prototype().description().set(name);

        NewGroupCommand context = newGroupBuilder.newInstance();
        interactions.newGroup(context);
    }

    @Then("groups named $name is added")
    public void thenGroupIsCreated(final String name)
    {
        ou.visitShared(new GroupVisitor()
        {
            public boolean visitGroup(Group groups)
            {
                ensureThat(groups.description().get(), equalTo(name));

                return true;
            }
        });
    }
*/
}