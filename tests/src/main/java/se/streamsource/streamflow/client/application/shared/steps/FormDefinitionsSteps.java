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
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.OrganizationSetupSteps;
import se.streamsource.streamflow.web.domain.form.FormDefinitionEntity;
import se.streamsource.streamflow.web.domain.form.FormDefinitions;

/**
 * JAVADOC
 */
public class FormDefinitionsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationSetupSteps organizationSetupSteps;

    public FormDefinitionEntity formDefinition;

    @When("form definition named $name is created")
    public void createForm(String name) throws Exception
    {
        try
        {
            FormDefinitions forms = organizationSetupSteps.organization;

            formDefinition = forms.createFormDefinition( name );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("form definition is removed")
    public void removed() throws Exception
    {
        try
        {
            FormDefinitions forms = organizationSetupSteps.organization;

            forms.removeFormDefinition( formDefinition );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }


}