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
import static org.hamcrest.core.IsEqual.equalTo;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.web.domain.task.*;

/**
 * JAVADOC
 */
public class InboxSteps
        extends Steps
{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    UserSteps userSteps;

    @Uses
    ProjectSteps projectSteps;

    @Uses
    OrganizationalUnitSteps organizationalUnitSteps;

    public Inbox inbox;
    public Inbox projectInbox;
    public Task task;

    @Given("an inbox")
    public void inbox() throws Exception
    {
        userSteps.givenUserNamed("administrator");
        inbox = userSteps.user;
    }

    @Given("a project inbox")
    public void projectNamed()
    {
        organizationalUnitSteps.givenOrganizationalUnit();
        projectSteps.newProject("newproject");
        projectInbox = projectSteps.project;
    }

    @When("a task is created")
    public void createTask()
    {
        task = inbox.createTask();
    }

    @When("a task is received by project inbox")
    public void projectReceiveTask()
    {
        projectInbox.receiveTask(task);
    }

    @Then("the task is created")
    public void taskIsCreated()
    {
        ensureThat(task, CoreMatchers.notNullValue());
    }

    @Then("the task is marked as $read")
    public void taskIsUnread(String read)
    {
        TaskEntity taskEntity = (TaskEntity) task;
        if (read.equals("read"))
        {
            ensureThat(taskEntity.isRead().get());
        } else if (read.equals("unread"))
        {
            ensureThat(!taskEntity.isRead().get());
        } else
        {
            ensureThat(false);
        }

    }

    @Then("the task is owned by project")
    public void ownedByProject()
    {
        TaskEntity taskEntity = (TaskEntity) task;
        ensureThat((Owner) projectInbox, equalTo(taskEntity.owner().get()));
    }


    @When("user completes the task")
    public void completeTask()
    {
        inbox.completeTask(task, userSteps.user);
    }

    @When("user drops the task")
    public void dropTask()
    {
        inbox.dropTask(task, userSteps.user);
    }

    @Then("the task state is $state")
    public void taskIsCompleted(String state)
    {
        TaskEntity taskEntity = (TaskEntity) task;
        taskEntity.status().get().equals(TaskStates.valueOf(state));
    }


    @When("user assigns the task to himself")
    public void assignTaskToUser()
    {
        inbox.assignTo(task, userSteps.user);
    }

    @Then("the task is assigned to user")
    public void taskIsAssigned()
    {
        TaskEntity taskEntity = (TaskEntity) task;
        ensureThat(userSteps.user, equalTo(taskEntity.assignedTo().get()));
    }

    @When("task is delegated from user to project")
    public void delegateTask()
    {
        inbox.delegateTo(task, (Delegatee) projectInbox, userSteps.user);
    }


    @Then("delegatedTo is project")
    public void delegatedToIsProject()
    {
        TaskEntity taskEntity = (TaskEntity) task;
        ensureThat((Delegatee) projectInbox, equalTo(taskEntity.delegatedTo().get()));
    }

    @Then("delegatedBy is user")
    public void delegatedByIsUser()
    {
        TaskEntity taskEntity = (TaskEntity) task;
        ensureThat(userSteps.user, equalTo(taskEntity.delegatedBy().get()));
    }

    @When("the task is set to $read")
    public void setTaskRead(String read)
    {
        if (read.equals("read"))
        {
            inbox.markAsRead(task);
        } else if (read.equals("unread"))
        {
            inbox.markAsUnread(task);
        } else
        {
            ensureThat(false);
        }
    }
}