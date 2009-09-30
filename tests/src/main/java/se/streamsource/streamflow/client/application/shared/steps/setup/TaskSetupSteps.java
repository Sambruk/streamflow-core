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
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class TaskSetupSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetupSteps;

    public Task assignedTask;
    public Task unassignedTask;
    public Assignments assignments;

    @Given("basic task setup")
    public void basicTaskSetup() throws Exception
    {
        userSetupSteps.basicUserSetup();
        UserEntity user = userSetupSteps.userMap.get("user1");
        unassignedTask = user.createTask();
        assignedTask = user.createTask();
        assignedTask.assignTo(user);
        assignments = userSetupSteps.userMap.get("user2");
    }

}