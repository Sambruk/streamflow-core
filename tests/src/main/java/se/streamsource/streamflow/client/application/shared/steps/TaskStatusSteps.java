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
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

/**
 * JAVADOC
 */
public class TaskStatusSteps
        extends Steps
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    @Uses
    UserSteps userSteps;

    @Uses
    InboxSteps inboxSteps;

    public TaskEntity task;
    private Exception taskException;

    @Given("a task with state $state")
    public void taskWithState(String state) throws Exception
    {
        inboxSteps.inbox();
        task = (TaskEntity) userSteps.user.createTask();

        task.status().set(TaskStates.valueOf(state));
    }

    @When("making task not assigned")
    public void makeNotAssigned()
    {
        task.unassign();
    }

    @When("task is completed")
    public void completeTask()
    {
        task.complete(userSteps.user);
    }

    @Then("task state is $state")
    public void checkTaskState(String state)
    {
        ensureThat(task.status().get(), CoreMatchers.equalTo(TaskStates.valueOf(state)));
    }

    @Then("task assignedTo has $value value")
    public void taskNotAssigned(String value)
    {
        TaskEntity taskEntity = (TaskEntity) task;

        if (value.equalsIgnoreCase("null"))
        {
            ensureThat(taskEntity.assignedTo().get(), CoreMatchers.nullValue());
        } else
        {
            ensureThat(taskEntity.assignedTo().get(), CoreMatchers.notNullValue());
        }
    }

    @When("setting task to $read")
    public void setTaskIsRead(String read)
    {
        TaskEntity taskEntity = (TaskEntity) task;

        taskEntity.isRead().set(read.equalsIgnoreCase("read"));
    }

    @Then("task is $read")
    public void taskIs(String read)
    {
        TaskEntity taskEntity = (TaskEntity) task;

        ensureThat(taskEntity.isRead().get(), CoreMatchers.equalTo(read.equalsIgnoreCase("read")));
    }

    @When("setting delegatedTo")
    public void setDelegateTo()
    {
        task.delegateTo(userSteps.user, userSteps.user, userSteps.user);
    }
}