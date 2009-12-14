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
import se.streamsource.streamflow.web.domain.form.FormTemplateEntity;

/**
 * JAVADOC
 */
public class FormTemplateSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   @Uses
   FormTemplatesSteps formTemplatesSteps;

   @Uses
   FieldDefinitionsSteps fieldDefinitionsSteps;

   /*
   @When("a field named $name is created in form")
   public void createField(String name) throws Exception
   {
       try
       {
           FormTemplateEntity form = formTemplatesSteps.givenTemplate;

           form.createField( name, valueDefinitionsSteps.givenValue );
       } catch(Exception e)
       {
           genericSteps.setThrowable(e);
       }
   }*/

   @When("a field is removed from form")
   public void removeField() throws Exception
   {
      try
      {
         FormTemplateEntity form = formTemplatesSteps.givenTemplate;

         form.removeField( fieldDefinitionsSteps.givenField );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }


}