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
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.TaskSetupSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.UserSetupSteps;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class AssignmentsSteps
        extends Steps
{
    @Uses
    TaskSetupSteps taskSetupSteps;

    @Uses
    UserSetupSteps userSetupSteps;

    @Uses
    GenericSteps genericSteps;


    @When("an assigned task is created for user named $name")
    public void createAssignedTask(String name)
    {
        UserEntity user = userSetupSteps.userMap.get(name);
        taskSetupSteps.assignments.createAssignedTask(user);
    }

    @When("$task assigned task is marked as $mark")
    public void markAssignedTaskAs(String task, String mark)
    {
        Task atask = "unread".equals(task) ? taskSetupSteps.unreadAssignedTask : taskSetupSteps.readAssignedTask;
        if ("read".equals(mark))
        {
            taskSetupSteps.assignments.markAssignedTaskAsRead(atask);
        } else
        {
            taskSetupSteps.assignments.markAssignedTaskAsUnread(atask);
        }
    }


    @When("assigned task is completed")
    public void complete()
    {
        taskSetupSteps.assignments.completeAssignedTask(taskSetupSteps.assignedTask);
    }


    @When("assigned task is dropped")
    public void drop()
    {
        taskSetupSteps.assignments.dropAssignedTask(taskSetupSteps.assignedTask);
    }

    @When("assigned task is forwarded")
    public void forward()
    {
        UserEntity user = userSetupSteps.userMap.get("user1");
        taskSetupSteps.assignments.forwardAssignedTask(taskSetupSteps.assignedTask, user);
    }


    @When("assigned task is delegated")
    public void delegate()
    {
        UserEntity user = userSetupSteps.userMap.get("user1");
        taskSetupSteps.assignments.delegateAssignedTaskTo(taskSetupSteps.assignedTask, user);
    }
}