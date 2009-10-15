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
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class OrganizationSetupSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetupSteps;

    @Uses
    GenericSteps genericSteps;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    EventSource source;

    public Map<String, OrganizationalUnit> orgUnitMap = new HashMap<String, OrganizationalUnit>();
    public OrganizationEntity organization;
    public Map<String, Project> projectMap = new HashMap<String, Project>();
    public GroupEntity group;

    @Given("basic organizational unit setup")
    public void setupOrganizationalUnit()
    {
        userSetupSteps.basicUserSetup();
        organization = getOrganization();

        OrganizationalUnitEntity ou1 = (OrganizationalUnitEntity) organization.createOrganizationalUnit("OU1");
        orgUnitMap.put("OU1", ou1);
        orgUnitMap.put("OU1Sub", ou1.createOrganizationalUnit("OU1Sub"));

        OrganizationalUnitEntity ou2 = (OrganizationalUnitEntity) organization.createOrganizationalUnit("OU2");
        orgUnitMap.put("OU2", ou2);
        orgUnitMap.put("OU2Sub",ou2.createOrganizationalUnit("OU2Sub"));
        projectMap.put("project1", ou2.createProject("project1"));
        projectMap.put("project2", ou2.createProject("project2"));

        group = ou2.createGroup("Group");
        group.addParticipant(userSetupSteps.userMap.get("user2"));

        genericSteps.clearEvents();
    }

    private OrganizationEntity getOrganization()
    {
        UserEntity user = userSetupSteps.userMap.get("user1");
        return (OrganizationEntity) user.organizations().iterator().next();
    }

}