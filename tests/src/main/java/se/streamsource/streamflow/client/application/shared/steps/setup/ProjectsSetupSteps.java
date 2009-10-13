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

package se.streamsource.streamflow.client.application.shared.steps.setup;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.project.Projects;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class ProjectsSetupSteps
        extends Steps
{
    @Uses
    OrganizationalUnitsSetupSteps orgSetup;

    @Uses
    UserSetupSteps userSetup;

    @Uses
    GenericSteps genericSteps;


    public Projects projects;
    public Map<String, ProjectEntity> projectMap = new HashMap<String, ProjectEntity>();

    @Given("basic projects setup")
    public void setupProjects() throws Exception
    {
        orgSetup.setupOrganizationalUnit();

        projects = (Projects) orgSetup.orgUnitMap.get("OU1");
        Projects projects2 = (Projects) orgSetup.orgUnitMap.get("OU2");

        ProjectEntity project = projects.createProject("project1");
        projectMap.put("project1", project);
        projectMap.put("project2", projects2.createProject("project2") );

        project.addMember(userSetup.userMap.get("user2"));

        genericSteps.clearEvents();
    }
}