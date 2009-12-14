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
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;

/**
 * JAVADOC
 */
public class GroupsSteps
      extends Steps
{
   @Uses
   OrganizationalUnitsSteps ouSteps;

   @Uses
   OrganizationsSteps organizationsSteps;

   public GroupEntity givenGroup;

   @Given("group named $name")
   public void givenGroup( String name )
   {
      givenGroup = ouSteps.givenOu.getGroupByName( name );
   }

   @When("a group named $name is created")
   public void createGroup( String name )
   {
      givenGroup = ouSteps.givenOu.createGroup( name );
   }

   @When("group is added")
   public void addGroup()
   {
      ouSteps.givenOu.addGroup( givenGroup );
   }

   @When("group is removed")
   public void removeGroup()
   {
      ouSteps.givenOu.removeGroup( givenGroup );
   }

   @When("groups are merged with $name")
   public void mergeGroups( String ouName )
   {
      Groups mergeToGroups = (Groups) organizationsSteps.givenOrganization.getOrganizationalUnitByName( ouName );
      ouSteps.givenOu.mergeGroups( mergeToGroups );
   }
}