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
import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;

/**
 * JAVADOC
 */
public class GroupSteps
        extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    @Uses
    OrganizationalUnitSteps organizationalUnitSteps;

    @Uses
    UserSteps userSteps;

    private DuplicateDescriptionException duplicateDescriptionException;

    @When("group named $name is created")
    public void newGroup(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;
        try
        {
            ouEntity.createGroup(name);
        } catch (DuplicateDescriptionException e)
        {
            duplicateDescriptionException = e;
        }
    }


    @When("group named $name is deleted")
    public void deleteGroup(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        Group group = findGroup(name);

        ensureThat(group, CoreMatchers.notNullValue());

        ouEntity.removeGroup(group);
    }

    @When("user is added as participant in group named $name")
    public void addUser(String name)
    {
        Group group = findGroup(name);

        ensureThat(group, CoreMatchers.notNullValue());

        group.addParticipant(userSteps.user);
    }

    @When("user is removed as participant from group named $name")
    public void removeUser(String name)
    {
        Group group = findGroup(name);

        ensureThat(group, CoreMatchers.notNullValue());

        group.removeParticipant(userSteps.user);
    }


    @Then("user $can be found in group named $groupName")
    public void userInGroup(String can, String groupName)
    {
        Group group = findGroup(groupName);

        ensureThat(group, CoreMatchers.notNullValue());

        GroupEntity groupEntity = (GroupEntity) group;

        if (can.equals("can"))
        {
            ensureThat(groupEntity.participants().contains(userSteps.user));
        } else if (can.equals("cannot"))
        {
            ensureThat(!groupEntity.participants().contains(userSteps.user));
        } else // fail
        {
            ensureThat(false);
        }
    }

    @Then("no DuplicateDescriptionException is thrown")
    public void noDDException()
    {
        ensureThat(duplicateDescriptionException, CoreMatchers.nullValue());
    }


    @Then("group named $name $can be found")
    public void groupAdded(String name, String can)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        boolean found = false;

        for (Group group : ouEntity.groups())
        {
            if (group.getDescription().equals(name))
            {
                found = true;
            }
        }

        if (can.equals("can"))
        {
            ensureThat(found);
        } else if (can.equals("cannot"))
        {
            ensureThat(!found);
        } else //fail
        {
            ensureThat(false);
        }


    }

    @Then("groupexception $name is thrown")
    public void exceptionThrown(String name)
    {
        ensureThat(duplicateDescriptionException.getClass().getSimpleName(), equalTo(name));

        duplicateDescriptionException = null;
    }

    private Group findGroup(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        for (Group group : ouEntity.groups())
        {
            if (group.getDescription().equals(name))
            {
                return group;
            }
        }

        return null;
    }

}