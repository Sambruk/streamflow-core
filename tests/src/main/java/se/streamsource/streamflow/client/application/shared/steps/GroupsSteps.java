/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.application.shared.steps.setup.GroupsSetupSteps;
import se.streamsource.streamflow.web.domain.group.GroupEntity;

/**
 * JAVADOC
 */
public class GroupsSteps
        extends Steps
{
    @Uses
    GroupsSetupSteps groupsSetup;

    @Structure
    ValueBuilderFactory vbf;

    @When("a new group with name $name is created")
    public void createGroup(String name)
    {
        groupsSetup.groups.createGroup(name);
    }

    @When("group named $name is added")
    public void addGroup(String name)
    {
        GroupEntity group = groupsSetup.groupMap.get(name);
        groupsSetup.groups.addGroup(group);
    }

    @When("group named $name is removed")
    public void removeGroup(String name)
    {
        GroupEntity group = groupsSetup.groupMap.get(name);
        groupsSetup.groups.removeGroup(group);
    }

    @When("groups are merged")
    public void mergeGroups()
    {
        groupsSetup.groups.mergeGroups(groupsSetup.toBeMerged); 
    }

}