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

import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.TestSetupSteps;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;

import static org.jbehave.Ensure.*;

/**
 * JAVADOC
 */
public class SubmittedFormsQueriesSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationsSteps organizationsSteps;

   @Uses
   UsersSteps usersSteps;

   @Uses
   SubmittedFormsSteps submittedFormsSteps;

   @Uses
   OrganizationalUnitsSteps ouSteps;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   FormsSteps formsSteps;

   @Uses
   InboxSteps inboxSteps;
   private SubmittedFormsListDTO submittedForms;
   private SubmittedFormDTO submittedForm;
   private EffectiveFieldsDTO effectiveFieldsDTO;


   @Given("basic form submit setup")
   public void setupFormSubmit() throws Exception
   {
      ouSteps.givenOrganization();
      ouSteps.givenOU( TestSetupSteps.OU1 );
      projectsSteps.givenProject( TestSetupSteps.PROJECT1 );

      formsSteps.givenForm( TestSetupSteps.SOME_FORM );
      usersSteps.givenUser( TestSetupSteps.USER1 );
      inboxSteps.createTask();
      submittedFormsSteps.createForm();
      //submittedFormsSteps.addFieldValueToForm( TestSetupSteps.SOME_FIELD, TestSetupSteps.SOME_VALUE );
      submittedFormsSteps.submissionDateIsNow();
      submittedFormsSteps.submitterIsSet();
      //submittedFormsSteps.submitForm();

      submittedFormsSteps.createForm();
      //submittedFormsSteps.addFieldValueToForm( TestSetupSteps.SOME_FIELD, TestSetupSteps.SOME_VALUE2 );
      submittedFormsSteps.submissionDateIsNow();
      submittedFormsSteps.submitterIsSet();
      //submittedFormsSteps.submitForm();

      genericSteps.clearEvents();
   }


   @Given("project task")
   public void givenProjectTask()
   {
      ensureThat( inboxSteps.givenTask, CoreMatchers.notNullValue() );
   }

   @When("submitted forms is requested")
   public void getSubmittedForms()
   {
      submittedForms = inboxSteps.givenTask.getSubmittedForms();
   }

   @Then("there are $expected submitted forms")
   public void countSubmittedForms( int expected )
   {
      ensureThat( submittedForms.forms().get().size(), CoreMatchers.equalTo( expected ) );
   }

   @When("submitted form $idx is requested")
   public void getSubmittedForm( int idx )
   {
      submittedForm = inboxSteps.givenTask.getSubmittedForm( idx );
   }

   @Then("is has name $name")
   public void ensureSubmittedFormName( String name )
   {
      ensureThat( submittedForm.form().get(), CoreMatchers.equalTo( name ) );
   }

   @Then("number of fields is $fieldCount")
   public void ensureFieldCount( int fieldCount )
   {
      ensureThat( submittedForm.values().get().size(), CoreMatchers.equalTo( fieldCount ) );
   }

   @Then("value of field number $fieldNumber is $fieldValue")
   public void ensureFieldValue( int fieldNumber, String fieldValue )
   {
      ensureThat( submittedForm.values().get().get( fieldNumber ).value().get(), CoreMatchers.equalTo( fieldValue ) );
   }

   @When("effective fields are requested")
   public void getEffectiveFields()
   {
      effectiveFieldsDTO = inboxSteps.givenTask.effectiveFields();
   }

   @Then("there is $fieldCount effective fields")
   public void countEffectiveFields( int fieldCount )
   {
      ensureThat( effectiveFieldsDTO.effectiveFields().get().size(), CoreMatchers.equalTo( fieldCount ) );
   }

   @Then("it has value $fieldValue")
   public void ensureOnlyEffectiveFieldValue( String fieldValue )
   {
      ensureThat( effectiveFieldsDTO.effectiveFields().get().get( 0 ).fieldValue().get(),
            CoreMatchers.equalTo( fieldValue ) );
   }

}