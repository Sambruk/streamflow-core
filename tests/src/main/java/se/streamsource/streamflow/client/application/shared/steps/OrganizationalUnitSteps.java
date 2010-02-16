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
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
public class OrganizationalUnitSteps
      extends Steps
{
   @Uses
   GenericSteps genericSteps;

   @Uses
   OrganizationalUnitsSteps ouSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   @When("organizational unit is moved to organizational unit named $ou2")
   public void move( String ou2 ) throws Exception
   {
      try
      {
         OrganizationalUnitRefactoring orgUnit1 = ouSteps.givenOu;
         OrganizationalUnits orgUnit2 = (OrganizationalUnits) orgsSteps.givenOrganization.getOrganizationalUnitByName( ou2 );
         orgUnit1.moveOrganizationalUnit( orgUnit2 );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   @When("organizational unit is merged to organizational unit named $ou2")
   public void merge( String ou2 ) throws Exception
   {
      try
      {
         OrganizationalUnitRefactoring orgUnit1 = ouSteps.givenOu;
         OrganizationalUnitRefactoring orgUnit2 = orgsSteps.givenOrganization.getOrganizationalUnitByName( ou2 );
         orgUnit1.mergeOrganizationalUnit( (OrganizationalUnit) orgUnit2 );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }

   @When("organizational unit is deleted")
   public void delete() throws Exception
   {
      try
      {
         OrganizationalUnitRefactoring orgUnit1 = ouSteps.givenOu;
         orgUnit1.deleteOrganizationalUnit();
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }


}