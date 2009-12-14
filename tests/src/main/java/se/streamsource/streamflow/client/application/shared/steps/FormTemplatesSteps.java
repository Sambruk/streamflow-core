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
import se.streamsource.streamflow.web.domain.form.FormTemplateEntity;
import se.streamsource.streamflow.web.domain.form.FormTemplates;

/**
 * JAVADOC
 */
public class FormTemplatesSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   FormsSteps formsSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   public FormTemplateEntity givenTemplate;

   @Given("form template named $form")
   public void givenFormTemplate( String name )
   {
      FormTemplates.Data forms = orgsSteps.givenOrganization;
      givenTemplate = forms.getTemplateByName( name );
   }

   @When("a form template is created")
   public void createTemplate() throws Exception
   {
      try
      {
         FormTemplates forms = orgsSteps.givenOrganization;

         givenTemplate = forms.createFormTemplate( formsSteps.givenForm );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   @When("a form template is removed")
   public void removeTemplate() throws Exception
   {
      try
      {
         FormTemplates forms = orgsSteps.givenOrganization;

         forms.removeFormTemplate( givenTemplate );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }
}