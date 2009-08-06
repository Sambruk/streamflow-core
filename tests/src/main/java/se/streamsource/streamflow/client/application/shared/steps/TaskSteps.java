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

import static org.hamcrest.core.IsEqual.equalTo;
import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import static org.jbehave.util.JUnit4Ensure.ensureThat;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.comment.CommentValue;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

import java.util.Date;

/**
 * JAVADOC
 */
public class TaskSteps
        extends Steps
{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    UserSteps userSteps;

    @Uses
    InboxSteps inboxSteps;

    public Task task;
    private Exception taskException;

    @Given("a task")
    public void task() throws Exception
    {
        inboxSteps.inbox();
        task = userSteps.user.newTask();
    }

    @When("setting comment with text $text")
    public void setComment(String text)
    {
        ValueBuilder<CommentValue> builder = vbf.newValueBuilder(CommentValue.class);
        builder.prototype().text().set(text);
        builder.prototype().creationDate().set(new Date());
        builder.prototype().commenter().set(EntityReference.getEntityReference(userSteps.user));
        task.addComment(builder.newInstance());
    }


    @Then("task has comment with text $text")
    public void findCommentWithText(String text)
    {
        TaskEntity taskEntity = (TaskEntity) task;

        boolean found = false;

        for (CommentValue comment : taskEntity.comments().get())
        {
            if (comment.text().get().equals(text))
            {
                found = true;
            }
        }
        ensureThat(found);
    }

    @When("setting description to $description")
    public void setTaskDescription(String description)
    {
        try
        {
            task.describe(description);
        } catch(Exception e)
        {
            taskException = e;
        }
    }

    @Then("task description is $description")
    public void checkTaskDescription(String description)
    {
        ensureThat(description, equalTo(task.getDescription()));
    }

    @Then("taskexception $name is thrown")
    public void taskExceptionThrown(String name)
    {
        System.out.println("########################"+taskException.getClass().getSimpleName());
        ensureThat(taskException.getClass().getSimpleName(), CoreMatchers.equalTo(name));

        taskException = null;

    }

    @Then("taskexception is not thrown")
    public void noTaskException()
    {
        ensureThat(taskException, CoreMatchers.nullValue());
    }
}