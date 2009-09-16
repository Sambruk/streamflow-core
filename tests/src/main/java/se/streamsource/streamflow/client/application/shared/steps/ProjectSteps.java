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

import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;

/**
 * JAVADOC
 */
public class ProjectSteps
        extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    @Uses
    OrganizationalUnitSteps organizationalUnitSteps;

    @Uses
    UserSteps userSteps;

    public Project project;

    @When("project named $name is created")
    public void newProject(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;
        project = ouEntity.createProject(name);
    }


    @When("project named $name is deleted")
    public void deleteGroup(String name) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        Project project = findProject(name);

        ensureThat(project, CoreMatchers.notNullValue());

        ouEntity.removeProject(project);

        uow.complete();
    }

    @When("user is added as member in project named $name")
    public void addUser(String name)
    {
        Project project = findProject(name);

        ensureThat(project, CoreMatchers.notNullValue());

        project.createMember(userSteps.user);
    }

    @When("user is removed as member from project named $name")
    public void removeUser(String name)
    {
        Project project = findProject(name);

        ensureThat(project, CoreMatchers.notNullValue());

        project.removeMember(userSteps.user);
    }


    @Then("user $can be found in project named $name")
    public void userInProject(String can, String name)
    {
        Project project = findProject(name);

        ensureThat(project, CoreMatchers.notNullValue());

        ProjectEntity projectEntity = (ProjectEntity) project;

        if (can.equals("can"))
        {
            ensureThat(projectEntity.members().get().getMemberValue(EntityReference.getEntityReference(userSteps.user)), CoreMatchers.notNullValue());
        } else if (can.equals("cannot"))
        {
            ensureThat(projectEntity.members().get().getMemberValue(EntityReference.getEntityReference(userSteps.user)), CoreMatchers.nullValue());
        } else // fail
        {
            ensureThat(false);
        }
    }

    @Then("project named $name $can be found")
    public void groupAdded(String name, String can)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        boolean found = false;

        for (Project project : ouEntity.projects())
        {
            if (project.getDescription().equals(name))
            {
                found = true;
            }
        }

        if (can.equals("can"))
        {
            ensureThat(found);
        } else if (can.equals("cannot"))
        {
            ensureThat(!found);
        } else //fail
        {
            ensureThat(false);
        }


    }

    private Project findProject(String name)
    {
        OrganizationalUnitEntity ouEntity = (OrganizationalUnitEntity) organizationalUnitSteps.ou;

        for (Project project : ouEntity.projects())
        {
            if (project.getDescription().equals(name))
            {
                return project;
            }
        }

        return null;
    }

}