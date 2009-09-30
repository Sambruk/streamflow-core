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
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class OrganizationalUnitsSetupSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetupSteps;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    EventSource source;

    public Map<String, OrganizationalUnit> orgUnitMap = new HashMap<String, OrganizationalUnit>();
    public OrganizationalUnitEntity parent;

    @Given("basic organizational unit setup")
    public void setupOrganizationalUnit() throws Exception
    {
        userSetupSteps.basicUserSetup();
        parent = getOrganizationalUnitEntity();
        orgUnitMap.put("OU1", parent.createOrganizationalUnit("UO1"));
        OrganizationalUnit uo2 = parent.createOrganizationalUnit("UO2");
        orgUnitMap.put("UO2", uo2);
        ((OrganizationalUnitEntity) uo2).createProject("Project");        
    }

    private OrganizationalUnitEntity getOrganizationalUnitEntity()
    {
        UserEntity user = userSetupSteps.userMap.get("user1");
        return (OrganizationalUnitEntity) user.organizations().iterator().next();
    }

}