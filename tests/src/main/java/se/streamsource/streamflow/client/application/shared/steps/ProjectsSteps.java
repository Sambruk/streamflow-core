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
import se.streamsource.streamflow.client.application.shared.steps.setup.ProjectsSetupSteps;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;

/**
 * JAVADOC
 */
public class ProjectsSteps
        extends Steps
{
    @Uses
    ProjectsSetupSteps projectsSetup;

    @When("a new project with name $name is created")
    public void createProject(String name)
    {
        projectsSetup.projects.createProject(name);
    }

    @When("project named $name is added")
    public void addProject(String name)
    {
        ProjectEntity project = projectsSetup.projectMap.get(name);
        projectsSetup.projects.addProject(project);
    }

    @When("project named $name is removed")
    public void removeProject(String name)
    {
        ProjectEntity project = projectsSetup.projectMap.get(name);
        projectsSetup.projects.removeProject(project);
    }
}