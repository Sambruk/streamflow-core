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

package se.streamsource.streamflow.client.application.shared.steps.setup;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class GroupsSetupSteps
        extends Steps
{
    @Uses
    OrganizationalUnitsSetupSteps orgSetup;

    @Uses
    UserSetupSteps userSetup;

    @Uses
    GenericSteps genericSteps;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    EventSource source;

    public Groups groups;
    public Groups toBeMerged;
    public Map<String, GroupEntity> groupMap = new HashMap<String, GroupEntity>();

    @Given("basic groups setup")
    public void setupGroups() throws Exception
    {
        orgSetup.setupOrganizationalUnit();

        groups = (Groups) orgSetup.orgUnitMap.get("OU1");
        toBeMerged = (Groups) orgSetup.orgUnitMap.get("OU2");

        GroupEntity group = groups.createGroup("group1");
        groupMap.put("group1", group);
        groupMap.put("group2", toBeMerged.createGroup("group2"));
        group.addParticipant(userSetup.userMap.get("user2"));

        genericSteps.clearEvents();
    }
}