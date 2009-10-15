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

package se.streamsource.streamflow.client.application.shared.steps.setup;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class TaskSetupSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetupSteps;

    @Uses
    GenericSteps genericSteps;

    @Uses
    OrganizationSetupSteps ouSteps;

    public Task assignedTask;
    public Task unassignedTask;
    public Task unreadAssignedTask;
    public Task readAssignedTask;
    public Task unreadInboxTask;
    public Task readInboxTask;
    public Task readWaitingForTask;
    public Task unreadWaitingForTask;

    public Assignments assignments;
    public Inbox inbox;
    public WaitingFor waitingFor;

    @Given("basic task setup")
    public void basicTaskSetup() throws Exception
    {
        ouSteps.setupOrganizationalUnit();

        UserEntity user = userSetupSteps.userMap.get("user1");
        unassignedTask = user.createTask();
        assignedTask = user.createTask();
        assignedTask.assignTo(user);
        inbox = user;
        unreadInboxTask = user.createTask();
        inbox.markAsUnread(unreadInboxTask);
        readInboxTask = user.createTask();

        assignments = userSetupSteps.userMap.get("user2");
        unreadAssignedTask = assignments.createAssignedTask(user);
        assignments.markAssignedTaskAsUnread(unreadAssignedTask);
        readAssignedTask = assignments.createAssignedTask(user);

        Project project = ouSteps.projectMap.get("project1");
        project.addMember(user);
        project.createTask();
        project.createTask().assignTo(user);

        project = ouSteps.projectMap.get("project2");
        ouSteps.projectMap.get("project2").addMember(ouSteps.group);
        project.createTask();
        project.createTask();

        waitingFor = userSetupSteps.userMap.get("user2");
        UserEntity user2 = userSetupSteps.userMap.get("user2");
        unreadWaitingForTask = user2.createTask();
        unreadWaitingForTask.delegateTo(user,user2, waitingFor);
        waitingFor.markWaitingForAsUnread(unreadWaitingForTask);

        readWaitingForTask = user2.createTask();
        readWaitingForTask.delegateTo(user, user2, waitingFor);

        genericSteps.clearEvents();
    }

}