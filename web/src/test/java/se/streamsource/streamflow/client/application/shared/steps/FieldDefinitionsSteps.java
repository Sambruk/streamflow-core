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

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldTemplates;

/**
 * JAVADOC
 */
public class FieldDefinitionsSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   public Field givenField;

   @Given("field definition named $name")
   public void givenFieldDefinition( String name )
   {
      givenField = orgsSteps.givenOrganization.getFieldDefinitionByName( name );
   }

/*
    @When("a field definition named $name of type $type is created")
    public void createField(String name, String type) throws Exception
    {
        try
        {
            FieldTemplates fields = orgsSteps.givenOrganization;

            this.givenField = fields.createFieldTemplate( field );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }
*/

   @When("a field definition namedis removed")
   public void removed() throws Exception
   {
      try
      {
         FieldTemplates.Data fieldsState = orgsSteps.givenOrganization;
         FieldTemplates fields = orgsSteps.givenOrganization;

         fields.removeFieldDefinition( givenField );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }


}