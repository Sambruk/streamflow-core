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

package se.streamsource.streamflow.client.model.shared;

import org.hamcrest.CoreMatchers;
import org.jbehave.Ensure;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.workspace.TaskGeneralModel;
import se.streamsource.streamflow.client.ui.workspace.UserInboxModel;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

/**
 * JAVADOC
 */
public class UserInboxSteps
        extends Steps
{
    @Structure
    ValueBuilderFactory vbf;

    @Service
    UserInboxModel model;

    @Uses
    IndividualSteps individualSteps;

    @Given("inbox")
    public void givenInbox() throws ResourceException
    {
//        model.setInbox(individualSteps.user().workspace().user().inbox());
    }

    @When("new task '$desc'")
    public void whenNewTask(String description) throws ResourceException
    {
        model.createTask();
        TaskGeneralModel generalModel = model.taskDetailModel(model.getTask(0).task().get().identity()).general();
        TaskGeneralDTO general = generalModel.getGeneral();
        ValueBuilder<TaskGeneralDTO> builder = general.buildWith();
        builder.prototype().description().set(description);
        generalModel.updateGeneral(builder.newInstance());
    }

    @Then("task count is $count")
    public void thenCount(int count)
    {
        Ensure.ensureThat(model.getRowCount(), CoreMatchers.equalTo(count));
    }
}
