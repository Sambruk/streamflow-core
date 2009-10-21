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

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.form.ValueDefinitionEntity;
import se.streamsource.streamflow.web.domain.form.ValueDefinitions;

/**
 * JAVADOC
 */
public class ValueDefinitionsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationsSteps orgsSteps;

    public ValueDefinitionEntity givenValue;

    @Given("value definition named $name")
    public void givenValue(String name)
    {
        ValueDefinitions.ValueDefinitionsState values = orgsSteps.givenOrganization;
        givenValue = values.getValueDefinitionByName( name );
    }

    @When("a value definition named $name is created")
    public void createValue(String name) throws Exception
    {
        try
        {
            ValueDefinitions values = orgsSteps.givenOrganization;

            givenValue = values.createValueDefinition( name );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("a value definition is removed")
    public void removed() throws Exception
    {
        try
        {
            ValueDefinitions values = orgsSteps.givenOrganization;

            values.removeValueDefinition( givenValue );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }


}