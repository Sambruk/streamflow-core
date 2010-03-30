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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;

/**
 * JAVADOC
 */
public class OrganizationsSteps
      extends Steps
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   GenericSteps genericSteps;

   public OrganizationsEntity organizations;

   public OrganizationEntity givenOrganization;
   public UserEntity givenUser;

   @Given("organization named $org")
   public void givenOrganization( String name )
   {
      givenOrganization = organizations.getOrganizationByName( name );
   }

   @Given("the organizations")
   public OrganizationsEntity givenOrganizations()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      organizations = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
      return organizations;
   }

   @When("a new organization named $name is created")
   public void createOrganization( String name ) throws Exception
   {
      try
      {
         givenOrganization = (OrganizationEntity) organizations.createOrganization( name );
      } catch (Exception e)
      {
         genericSteps.setThrowable( e );
      }
   }
}