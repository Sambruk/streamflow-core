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

import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.OrganizationalUnitsSetupSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.client.test.ErrorHandlingConcern;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;

/**
 * JAVADOC
 */
@Concerns(ErrorHandlingConcern.class)
public class OrganizationalUnitsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationalUnitsSetupSteps organizationalUnitsSetupSteps;

    @When("an organizational unit named $name is created")
    public void createOrganizationalUnit(String name) throws Exception
    {
        genericSteps.clearEvents();
        try
        {
            organizationalUnitsSetupSteps.parent.createOrganizationalUnit(name);
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("organizational unit named $name is removed")
    public void removeOrgUnit(String name)
    {
        genericSteps.clearEvents();
        try
        {
            OrganizationalUnitEntity ou = (OrganizationalUnitEntity) organizationalUnitsSetupSteps.orgUnitMap.get(name);
            organizationalUnitsSetupSteps.parent.removeOrganizationalUnit(ou);
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }
}