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
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.project.Project;

/**
 * JAVADOC
 */
public class ParticipantSteps
        extends Steps
{
    @Uses
    ProjectsSteps projectsSteps;

    @Uses
    GroupsSteps groupsSteps;

    @Uses
    OrganizationsSteps orgsSteps;

    @When("a participant joins a project")
    public void joinProject()
    {
        Project project = projectsSteps.givenProject;
        orgsSteps.givenUser.joinProject(project);
    }

    @When("a participant leaves a project")
    public void leaveProject()
    {
        Project project = projectsSteps.givenProject;
        orgsSteps.givenUser.leaveProject(project);
    }

    @When("a participant joins a group")
    public void joinGroup()
    {
        Group group = groupsSteps.givenGroup;
        orgsSteps.givenUser.joinGroup(group);
    }

    @When("a participant leaves a group")
    public void leaveGroup()
    {
        Group group = groupsSteps.givenGroup;
        orgsSteps.givenUser.leaveGroup(group);
    }
}