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

import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.form.FieldDefinitionEntity;
import se.streamsource.streamflow.web.domain.form.FieldValue;
import se.streamsource.streamflow.web.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.form.SubmittedForms;

/**
 * JAVADOC
 */
public class SubmittedFormsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationsSteps organizationsSteps;

    @Uses
    ProjectFormDefinitionsSteps projectFormDefinitionsSteps;

    @Uses
    InboxSteps inboxSteps;

    @Structure
    ValueBuilderFactory vbf;

    public SubmittedFormValue form;

    @When("form submission is created")
    public void createForm()
    {
        form = vbf.newValueBuilder( SubmittedFormValue.class ).prototype();
    }

    @When("field $name with value $value is added to form")
    public void addFieldValueToForm(String field, String value)
    {
        ValueBuilder<FieldValue> builder = vbf.newValueBuilder( FieldValue.class );

        FieldDefinitionEntity entity = projectFormDefinitionsSteps.givenForm.getFieldByName( field );
        builder.prototype().field().set( EntityReference.getEntityReference( entity ));
        builder.prototype().value().set( value );

        form.values().get().add( builder.newInstance() );
    }

    @When("form is submitted")
    public void submitForm() throws Exception
    {
        try
        {
            inboxSteps.task.submitForm( form );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @Then("effective field value for field $field is $value")
    public void removed(String form, String value) throws Exception
    {
        SubmittedForms.SubmittedFormsState submittedFormsState = inboxSteps.task;

/*


        submittedFormsState.getEffectiveValue(  )

        Ensure.ensureThat();

        try
        {
            FormDefinitions.FormDefinitionsState forms = organizationSetupSteps.organization;


            FormDefinition formDefinition = forms.getFormByName( form );

            submittedFormsState.removeFormDefinition( formDefinition );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
*/
    }


}