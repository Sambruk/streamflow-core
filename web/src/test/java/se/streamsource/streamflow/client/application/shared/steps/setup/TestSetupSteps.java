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
import se.streamsource.streamflow.client.application.shared.steps.FormsSteps;
import se.streamsource.streamflow.client.application.shared.steps.GroupsSteps;
import se.streamsource.streamflow.client.application.shared.steps.InboxSteps;
import se.streamsource.streamflow.client.application.shared.steps.MembersSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationalUnitsSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ParticipantsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ProjectsSteps;
import se.streamsource.streamflow.client.application.shared.steps.SubmittedFormsSteps;
import se.streamsource.streamflow.client.application.shared.steps.TaskTypesSteps;
import se.streamsource.streamflow.client.application.shared.steps.UsersSteps;
import se.streamsource.streamflow.test.GenericSteps;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;

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

   public static final String TASKTYPE1 = "tasktype1";
   public static final String TASKTYPE2 = "tasktype2";

   public static final String PROJECT1 = "project1";
   public static final String PROJECT2 = "project2";

   public static final String OU1 = "OU1";
   public static final String OU1_SUB = "OU1Sub";
   public static final String OU2 = "OU2";
   public static final String OU2_SUB = "OU2Sub";

   public static final String GROUP1 = "group1";
   public static final String GROUP2 = "group2";

   public static final String SOME_VALUE = "SomeValue";
   public static final String SOME_VALUE2 = "SomeValue2";
   public static final String SOME_FIELD = "SomeField";
   public static final String SOME_FIELD2 = "SomeField2";
   public static final String SOME_FORM = "SomeForm";

   @Optional
   @Uses
   ProjectsSteps projectsSteps;

   @Optional
   @Uses
   TaskTypesSteps taskTypesSteps;

   @Optional
   @Uses
   FieldDefinitionsSteps fieldDefinitionsSteps;

   @Optional
   @Uses
   FormsSteps formsSteps;

   @Optional
   @Uses
   InboxSteps inboxSteps;

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
   UsersSteps usersSteps;

   @Optional
   @Uses
   SubmittedFormsSteps submittedFormsSteps;

   @Optional
   @Uses
   GenericSteps genericSteps;

   @Structure
   UnitOfWorkFactory uowf;

   public TaskEntity assignedTask;
   public TaskEntity unassignedTask;
   public TaskEntity readAssignedTask;
   public TaskEntity readInboxTask;
   public TaskEntity readWaitingForTask;
   public TaskEntity readDelegatedTask;

   @Given("basic setup 1")
   public void setup1() throws Exception
   {
      setupOrganizationalUnit();
      setupUsers();
      setupTaskTypes();
      setupForms();
      setupGroups();
      setupProjects();
      setupTasks();
   }

   @Given("basic tasktype setup")
   public void setupTaskTypes()
   {
      organizationsSteps.givenOrganization( "Organization" );
      taskTypesSteps.createTaskType( TASKTYPE1 );
      taskTypesSteps.createTaskType( TASKTYPE2 );
   }

   @Given("basic form setup")
   public void setupForms() throws Exception
   {
      ouSteps.givenOrganization();
      ouSteps.givenOU( OU1 );
      taskTypesSteps.givenTaskType( TASKTYPE1 );

      formsSteps.createForm( SOME_FORM );
      //formsSteps.createField( SOME_FIELD );
      //formsSteps.createField( SOME_FIELD );

      genericSteps.clearEvents();
   }

   @Given("basic group setup")
   public void setupGroups() throws Exception
   {
      organizationsSteps.givenOrganization( "Organization" );
      ouSteps.givenOrganization();
      ouSteps.givenOU( OU1 );
      usersSteps.givenUser( USER1 );
      groupsSteps.createGroup( GROUP1 );
      participantsSteps.joinGroup();
      ouSteps.givenOrganization();
      ouSteps.givenOU( OU2 );
      groupsSteps.createGroup( GROUP2 );
      usersSteps.givenUser( USER2 );
      participantsSteps.joinGroup();

      genericSteps.clearEvents();
   }

   @Given("basic organizational unit setup")
   public void setupOrganizationalUnit() throws UnitOfWorkCompletionException
   {
      organizationsSteps.givenOrganizations();
      organizationsSteps.givenOrganization( "Organization" );
      ouSteps.givenOrganizationalUnits = organizationsSteps.givenOrganization;
      ouSteps.createOrganizationalUnit( OU1 );
      ouSteps.createOrganizationalUnit( OU1_SUB );

      ouSteps.givenOrganizationalUnits = organizationsSteps.givenOrganization;

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
      usersSteps.givenUser( USER1 );
      membersSteps.addMember();

      genericSteps.clearEvents();
   }

   @Given("basic task setup")
   public void setupTasks() throws Exception
   {
      UserEntity user = usersSteps.givenUsers().getUserByName( USER1 );
      unassignedTask = user.createTask();
      assignedTask = user.createTask();
      assignedTask.assignTo( user );
      Inbox inbox = user;
      readInboxTask = user.createTask();

      UserEntity user2 = usersSteps.givenUsers().getUserByName( USER2 );
      readAssignedTask = user2.createTask();
      readAssignedTask.assignTo( user2 );

      ouSteps.givenOrganization();
      ouSteps.givenOU( OU1 );
      projectsSteps.givenProject( PROJECT1 );
      ProjectEntity project = projectsSteps.givenProject;
      project.addMember( user );
      project.createTask();
      project.createTask().assignTo( user );

      ouSteps.givenOrganization();
      ouSteps.givenOU( OU2 );
      groupsSteps.givenGroup( GROUP2 );
      projectsSteps.givenProject( PROJECT2 );
      project = projectsSteps.givenProject;
      project.addMember( groupsSteps.givenGroup );
      project.createTask();
      project.createTask();

      readWaitingForTask = project.createTask();
      readWaitingForTask.delegateTo( user, user2, project );

      genericSteps.clearEvents();
   }

   @Given("basic user setup")
   public void setupUsers() throws Exception
   {
      Users users = usersSteps.givenUsers();
      organizationsSteps.givenOrganization( "Organization" );
      users.createUser( USER1, "password1" ).join( organizationsSteps.givenOrganization );
      users.createUser( USER2, "password2" ).join( organizationsSteps.givenOrganization );
      User entity = users.createUser( DISABLED_USER, "password3" );

      entity.join( organizationsSteps.givenOrganization );
      entity.changeEnabled( false );
      entity = users.createUser( USER_NOT_IN_AN_ORGANIZATION, "password4" );
      genericSteps.clearEvents();
   }
}
