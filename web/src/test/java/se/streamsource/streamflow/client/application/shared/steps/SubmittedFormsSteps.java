/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

import java.util.Date;

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
   FormsSteps formsSteps;

   @Uses
   InboxSteps inboxSteps;

   @Structure
   ValueBuilderFactory vbf;

   public SubmittedFormValue form;
   public ValueBuilder<SubmittedFormValue> formBuilder;

   @When("form submission is created")
   public void createForm()
   {
      formBuilder = vbf.newValueBuilder( SubmittedFormValue.class );
      form = formBuilder.prototype();
      form.form().set( EntityReference.getEntityReference( formsSteps.givenForm ) );
   }
/*
   @When("field $name with value $value is added to form")
   public void addFieldValueToForm( String field, String value )
   {
      ValueBuilder<SubmittedFieldValue> builder = vbf.newValueBuilder( SubmittedFieldValue.class );

      Field entity = formsSteps.givenForm.getFieldByName( field );
      builder.prototype().field().set( EntityReference.getEntityReference( entity ) );
      builder.prototype().value().set( value );

      form.values().get().add( builder.newInstance() );
   }
*/
   @When("submission date is now")
   public void submissionDateIsNow()
   {
      form.submissionDate().set( new Date() );
   }

   @When("submitter is set")
   public void submitterIsSet()
   {
      form.submitter().set( EntityReference.getEntityReference( organizationsSteps.givenUser ) );
   }

   /*
   @When("form is submitted")
   public void submitForm() throws Exception
   {

      form.submissionDate().set( new Date() );

      try
      {
         inboxSteps.givenTask.submitForm( formBuilder.newInstance() );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }*/

   @Then("effective field value for field $field is $value")
   public void removed( String form, String value ) throws Exception
   {
      SubmittedForms.Data data = (SubmittedForms.Data) inboxSteps.givenTask;

/*


        data.getEffectiveValue(  )

        Ensure.ensureThat();

        try
        {
            FormDefinitions.Data forms = organizationSetupSteps.organization;


            Form formDefinition = forms.getFormByName( form );

            data.removeFormDefinition( formDefinition );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
*/
   }


}