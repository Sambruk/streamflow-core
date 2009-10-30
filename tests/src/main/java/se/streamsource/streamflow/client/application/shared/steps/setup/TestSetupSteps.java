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
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.application.shared.steps.FieldDefinitionsSteps;
import se.streamsource.streamflow.client.application.shared.steps.FormTemplateSteps;
import se.streamsource.streamflow.client.application.shared.steps.FormTemplatesSteps;
import se.streamsource.streamflow.client.application.shared.steps.FormsSteps;
import se.streamsource.streamflow.client.application.shared.steps.GroupsSteps;
import se.streamsource.streamflow.client.application.shared.steps.MembersSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationalUnitsSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ParticipantsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ProjectsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ValueDefinitionsSteps;
import se.streamsource.streamflow.web.domain.organization.Organizations;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class TestSetupSteps
    extends Steps
{
    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    public static final String DISABLED_USER = "disabledUser";
    public static final String USER_NOT_IN_AN_ORGANIZATION = "userNotInAnOrganization";

    public static final String PROJECT1 = "project1";
    public static final String PROJECT2 = "project2";

    public static final String OU1 = "OU1";
    public static final String OU1_SUB = "OU1Sub";
    public static final String OU2 = "OU2";
    public static final String OU2_SUB = "OU2Sub";

    public static final String GROUP1 = "group1";
    public static final String GROUP2 = "group2";

    public static final String SOME_VALUE = "SomeValue";
    public static final String SOME_FIELD = "SomeField";
    public static final String SOME_FIELD2 = "SomeField2";
    public static final String SOME_FORM = "SomeForm";

    @Optional
    @Uses
    ProjectsSteps projectsSteps;

    @Optional
    @Uses
    ValueDefinitionsSteps valueDefinitionsSteps;

    @Optional
    @Uses
    FieldDefinitionsSteps fieldDefinitionsSteps;

    @Optional
    @Uses
    FormTemplatesSteps formTemplatesSteps;

    @Optional
    @Uses
    FormTemplateSteps formTemplateSteps;

    @Optional
    @Uses
    FormsSteps formsSteps;

    @Optional
    @Uses
    OrganizationalUnitsSteps ouSteps;

    @Optional
    @Uses
    GroupsSteps groupsSteps;

    @Optional
    @Uses
    MembersSteps membersSteps;

    @Optional
    @Uses
    ParticipantsSteps participantsSteps;

    @Optional
    @Uses
    OrganizationsSteps organizationsSteps;

    @Optional
    @Uses
    GenericSteps genericSteps;

    @Structure
    UnitOfWorkFactory uowf;

    public Task assignedTask;
    public Task unassignedTask;
    public Task unreadAssignedTask;
    public Task readAssignedTask;
    public Task unreadInboxTask;
    public Task readInboxTask;
    public Task readWaitingForTask;
    public Task unreadWaitingForTask;

    @Given("basic setup 1")
    public void setup1() throws Exception
    {
        setupOrganizationalUnit();
        setupUsers();
        setupGroups();
        setupProjects();
        setupTasks();
        setupForms();
    }

    @Given("basic form setup")
    public void setupForms() throws Exception
    {
        valueDefinitionsSteps.createValue( SOME_VALUE );

        formTemplatesSteps.createForm( SOME_FORM);
        fieldDefinitionsSteps.createField( SOME_FIELD, SOME_VALUE );
        formTemplateSteps.createField(SOME_FIELD );
        fieldDefinitionsSteps.createField( SOME_FIELD2, SOME_VALUE );
        formTemplateSteps.createField(SOME_FIELD);

        ouSteps.givenOrganization();
        ouSteps.givenOU( "OU1" );
        projectsSteps.givenProject( "project1" );


        formsSteps.addForm( );

        genericSteps.clearEvents();
    }

    @Given("basic group setup")
    public void setupGroups() throws Exception
    {
        ouSteps.givenOrganization();
        ouSteps.givenOU( OU1 );
        organizationsSteps.givenUser( USER1 );
        groupsSteps.createGroup( GROUP1 );
        participantsSteps.joinGroup();
        ouSteps.givenOrganization();
        ouSteps.givenOU( OU2 );
        groupsSteps.createGroup( GROUP2 );
        organizationsSteps.givenUser( USER2 );
        participantsSteps.joinGroup();

        genericSteps.clearEvents();
    }

    @Given("basic organizational unit setup")
    public void setupOrganizationalUnit() throws UnitOfWorkCompletionException
    {
        organizationsSteps.givenOrganizations();
        organizationsSteps.givenOrganization("Organization");
        ouSteps.givenOu = organizationsSteps.givenOrganization;
        ouSteps.createOrganizationalUnit( OU1 );
        ouSteps.createOrganizationalUnit( OU1_SUB );

        ouSteps.givenOu = organizationsSteps.givenOrganization;

        ouSteps.createOrganizationalUnit( OU2 );

        ouSteps.createOrganizationalUnit( OU2_SUB );

        genericSteps.clearEvents();
    }

    @Given("basic project setup")
    public void setupProjects() throws Exception
    {
        ouSteps.givenOrganization();
        ouSteps.givenOU( OU1 );
        projectsSteps.createProject( PROJECT1 );

        ouSteps.givenOrganization();
        ouSteps.givenOU( OU2 );
        projectsSteps.createProject( PROJECT2 );

        ouSteps.givenOrganization();
        ouSteps.givenOU( OU1 );
        projectsSteps.givenProject( PROJECT1 );
        organizationsSteps.givenUser( USER1 );
        membersSteps.addMember();

        genericSteps.clearEvents();
    }

    @Given("basic task setup")
    public void setupTasks() throws Exception
    {
        UserEntity user = organizationsSteps.givenOrganizations().getUserByName(USER1);
        unassignedTask = user.createTask();
        assignedTask = user.createTask();
        assignedTask.assignTo(user);
        Inbox inbox = user;
        unreadInboxTask = user.createTask();
        inbox.markAsUnread(unreadInboxTask);
        readInboxTask = user.createTask();

        Assignments assignments = organizationsSteps.givenOrganizations().getUserByName(USER2);
        unreadAssignedTask = assignments.createAssignedTask(user);
        assignments.markAssignedTaskAsUnread(unreadAssignedTask);
        readAssignedTask = assignments.createAssignedTask(user);

        ouSteps.givenOrganization();
        ouSteps.givenOU( OU1 );
        projectsSteps.givenProject( PROJECT1 );
        Project project = projectsSteps.givenProject;
        project.addMember(user);
        project.createTask();
        project.createTask().assignTo(user);

        ouSteps.givenOrganization();
        ouSteps.givenOU( OU2 );
        groupsSteps.givenGroup(GROUP2);
        projectsSteps.givenProject( PROJECT2 );
        project = projectsSteps.givenProject;
        project.addMember(groupsSteps.givenGroup);
        project.createTask();
        project.createTask();

        WaitingFor waitingFor = project;
        UserEntity user2 = organizationsSteps.givenOrganizations().getUserByName(USER2);
        unreadWaitingForTask = project.createTask();
        project.delegateTo( unreadWaitingForTask, project, user2 );
        waitingFor.markWaitingForAsUnread(unreadWaitingForTask);

        readWaitingForTask = project.createTask();
        project.delegateTo( readWaitingForTask, project, user2 );
        readWaitingForTask.delegateTo(user, user2, waitingFor);

        genericSteps.clearEvents();
    }

    @Given("basic user setup")
    public void setupUsers() throws Exception
    {
        Organizations organizations = organizationsSteps.givenOrganizations();
        organizationsSteps.givenOrganization( "Organization" );
        organizations.createUser( USER1, "password1").join( organizationsSteps.givenOrganization );
        organizations.createUser( USER2, "password2").join( organizationsSteps.givenOrganization );
        UserEntity entity = organizations.createUser( DISABLED_USER, "password3" );

        entity.join( organizationsSteps.givenOrganization );
        entity.changeEnabled( false );
        entity = organizations.createUser( USER_NOT_IN_AN_ORGANIZATION, "password4" );
        genericSteps.clearEvents();
    }
}
