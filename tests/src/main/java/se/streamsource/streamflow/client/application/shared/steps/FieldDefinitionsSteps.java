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
import se.streamsource.streamflow.web.domain.form.FieldDefinition;
import se.streamsource.streamflow.web.domain.form.FieldDefinitionEntity;
import se.streamsource.streamflow.web.domain.form.FieldDefinitions;
import se.streamsource.streamflow.web.domain.form.ValueDefinition;
import se.streamsource.streamflow.web.domain.form.ValueDefinitions;

/**
 * JAVADOC
 */
public class FieldDefinitionsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationSetupSteps organizationSetupSteps;

    public FieldDefinitionEntity fieldDefinition;

    @When("field definition named $name of type $type is created")
    public void createField(String name, String type) throws Exception
    {
        try
        {
            FieldDefinitions fields = organizationSetupSteps.organization;

            ValueDefinitions.ValueDefinitionsState values = organizationSetupSteps.organization;

            ValueDefinition valueDefinition = values.getValueDefinitionByName( type );

            this.fieldDefinition = fields.createFieldDefinition( name, valueDefinition );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("field definition named $name is removed")
    public void removed(String name) throws Exception
    {
        try
        {
            FieldDefinitions.FieldDefinitionsState fieldsState = organizationSetupSteps.organization;
            FieldDefinitions fields = organizationSetupSteps.organization;

            FieldDefinition fieldDefinition = fieldsState.getFieldDefinitionByName( name );

            fields.removeFieldDefinition( fieldDefinition );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }


}