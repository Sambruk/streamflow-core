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

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
public class OrganizationalUnitsSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationsSteps organizationsSteps;

   public OrganizationalUnits givenOrganizationalUnits;

   public OrganizationalUnitEntity givenOu;

   @Given("the organization")
   public void givenOrganization()
   {
      givenOrganizationalUnits = organizationsSteps.givenOrganization;
   }

   @Given("organizational unit named $name")
   public void givenOU( String name )
   {
      givenOrganizationalUnits = ((OrganizationalUnits.Data) givenOrganizationalUnits).getOrganizationalUnitByName( name );
      givenOu = (OrganizationalUnitEntity) givenOrganizationalUnits;
   }

   @When("an organizational unit named $name is created")
   public void createOrganizationalUnit( String name )
   {
      try
      {
         givenOrganizationalUnits = givenOrganizationalUnits.createOrganizationalUnit( name );
         givenOu = (OrganizationalUnitEntity) givenOrganizationalUnits;
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   @When("an organizational unit named $name is removed")
   public void removeOrgUnit( String name )
   {
      try
      {
         OrganizationalUnits organizationalUnits = ((OrganizationalUnitRefactoring.Data) givenOrganizationalUnits).getParent();
         OrganizationalUnit removeOu = ((OrganizationalUnits.Data) organizationalUnits).getOrganizationalUnitByName( name );
         organizationalUnits.removeOrganizationalUnit( removeOu );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }
}