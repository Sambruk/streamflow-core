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
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.project.Role;

/**
 * JAVADOC
 */
public class GroupSteps
        extends Steps
{
    @Uses
    UserSteps user;

    @Uses
    OrganizationalUnitSteps organization;

    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    IndividualRepository individualRepository;

    public Group group;
    public Role role;
    private Exception thrownException;

/*
    @Given("a groups named $name")
    public void givenAGroup(String name) throws Exception
    {
        createGroup(name);
    }

    @Given("a role named $name")
    public void givenARole(String name) throws Exception
    {
        EntityBuilder<Role> roleBuilder = uowf.currentUnitOfWork().newEntityBuilder(Role.class);
        roleBuilder.prototypeFor(Describable.class).description().set(name);
        role = roleBuilder.newInstance();

        ValueBuilder<NewRoleContext> newRoleBuilder = vbf.newValueBuilder(NewRoleContext.class);
        newRoleBuilder.prototype().context().set(organizations.html.ou);
        newRoleBuilder.prototype().description().set(name);
        NewRoleContext context = newRoleBuilder.newInstance();

        role = interactions.newRole(context);
    }

    @When("users is added as participant")
    public void userIsAddedAsParticipant() throws Exception
    {
        try
        {
            ValueBuilder<AddParticipantContext> addParticipantBuilder = vbf.newValueBuilder(AddParticipantContext.class);
            addParticipantBuilder.prototype().context().set(groups);
            addParticipantBuilder.prototype().participant().set(users.users);
            AddParticipantContext context = addParticipantBuilder.newInstance();

            interactions.addParticipant(context);
        } catch (Exception e)
        {
            thrownException = e;
        }
    }

    @When("groups named $name is created")
    public void createGroup(String name) throws Exception
    {
        try
        {
            ValueBuilder<NewGroupCommand> newGroupBuilder = vbf.newValueBuilder(NewGroupCommand.class);
            newGroupBuilder.prototype().context().set(organizations.html.ou);
            newGroupBuilder.prototype().description().set(name);
            NewGroupCommand context = newGroupBuilder.newInstance();

            groups = interactions.newGroup(context);

        } catch (Exception e)
        {
            thrownException = e;
        }
    }

    @Then("groups named $name is added")
    public void thenGroupIsCreated(final String name)
    {
        organizations.html.ou.visitShared(new GroupVisitor()
        {
            public boolean visitGroup(Group groups)
            {
                ensureThat(groups.description().get(), equalTo(name));

                return true;
            }
        });
    }

    @Then("exception is not thrown")
    public void thenExceptionIsNotThrown()
    {
        ensureThat(thrownException ==null);
    }

    @Then("$exceptionName is thrown")
    public void thenExceptionIsThrown(final String exceptionName)
    {
        ensureThat(thrownException !=null);
        ensureThat(thrownException.getClass().getSimpleName(), equalTo(exceptionName));
    }


    @Then("participant is added")
    public void thenParticipantIsAdded()
    {
        ensureThat(groups.isParticipant(users.users));
    }
*/
}
