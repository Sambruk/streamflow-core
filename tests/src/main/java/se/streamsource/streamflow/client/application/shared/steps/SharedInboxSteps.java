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

import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

/**
 * JAVADOC
 */
public class SharedInboxSteps
        extends Steps
{
    @Structure
    ValueBuilderFactory vbf;

    @Service
    IndividualRepository individualRepository;

    private String result;
    private Account account;

/*
    @Given("an inbox")
    public void sharedInbox() throws AssemblyException
    {
        navigator.individual().visitShared(new InboxVisitor()
        {
            public boolean visitInbox(Inbox sharedInbox)
            {
                inbox = sharedInbox;
                return false;
            }
        });
    }

    @Given("a workspace task")
    public void sharedTask() throws Exception
    {
        newSharedTask();
    }

    @When("workspace task is created")
    public void newSharedTask() throws Exception
    {
        ValueBuilder<NewTaskCommand> contextBuilder = vbf.newValueBuilder(NewTaskCommand.class);
        contextBuilder.prototype().context().set(inbox);
        contextBuilder.prototype().description().set("New workspace task");
        sharedTask = sharedInteractions.newSharedTask(contextBuilder.newInstance());
    }

    @When("workspace task is completed")
    public void completeSharedTask() throws Exception
    {
        ValueBuilder<CompleteInboxTaskCommand> contextBuilder = vbf.newValueBuilder(CompleteInboxTaskCommand.class);
        contextBuilder.prototype().context().set(inbox);
        contextBuilder.prototype().sharedTask().set(sharedTask);
        sharedInteractions.completeSharedTask(contextBuilder.newInstance());
    }

    @Then("task is completed")
    public void thenTaskIsCompleted()
    {
        ensureThat(sharedTask.status(), equalTo(TaskStates.COMPLETED));
    }

    @Then("task is added")
    public void taskAdded()
    {
        final Task[] tasks = new Task[1];
        inbox.visitTasks(new SharedTaskVisitable.SharedTaskVisitor()
        {
            public boolean visit(Task task)
            {
                tasks[0] = task;
                return false;
            }
        });

        ensureThat(tasks[0], CoreMatchers.notNullValue());
    }
*/
}