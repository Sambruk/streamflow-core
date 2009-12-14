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
import se.streamsource.streamflow.web.domain.project.ProjectEntity;

/**
 * JAVADOC
 */
public class ProjectsSteps
      extends Steps
{
   @Uses
   OrganizationalUnitsSteps ouSteps;

   public ProjectEntity givenProject;


   @Given("project named $project")
   public void givenProject( String name )
   {
      givenProject = ouSteps.givenOu.getProjectByName( name );
   }

   @When("a project named $name is created")
   public void createProject( String name )
   {
      givenProject = ouSteps.givenOu.createProject( name );
   }

   @When("project is added")
   public void addProject()
   {
      ouSteps.givenOu.addProject( givenProject );
   }

   @When("project is removed")
   public void removeProject()
   {
      ouSteps.givenOu.removeProject( givenProject );
   }
}