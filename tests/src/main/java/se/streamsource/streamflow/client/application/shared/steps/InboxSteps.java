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

/**
 * JAVADOC
 */
public class InboxSteps
        extends Steps
{
    @Uses
    TaskSetupSteps taskSetupSteps;

    @Uses
    UserSetupSteps userSetupSteps;

    @Uses
    GenericSteps genericSteps;

    @When("inbox task is created")
    public void createInboxTask()
    {
        taskSetupSteps.inbox.createTask();
    }

    @When("$task inbox task is marked as $mark")
    public void markAssignedTaskAs(String task, String mark)
    {
        Task atask = "unread".equals(task) ? taskSetupSteps.unreadInboxTask : taskSetupSteps.readInboxTask;
        if ("read".equals(mark))
        {
            taskSetupSteps.inbox.markAsRead(atask);
        } else
        {
            taskSetupSteps.inbox.markAsUnread(atask);
        }
    }

    /*
    void receiveTask(Task task);

    void completeTask(Task task, Assignee assignee);

    void dropTask(Task task, Assignee assignee);

    void assignTo(Task task, Assignee assignee);

    void delegateTo(Task task, Delegatee delegatee, Delegator delegator);

     */
}