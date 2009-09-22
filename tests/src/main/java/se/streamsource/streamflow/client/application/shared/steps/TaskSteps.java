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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.comment.CommentValue;
import se.streamsource.streamflow.web.domain.label.Label;
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

    @Structure
    ObjectBuilderFactory obf;

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
        task = userSteps.user.createTask();
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
            task.changeDescription(description);
        } catch (Exception e)
        {
            taskException = e;
        }
    }

    @Then("task description is $description")
    public void checkTaskDescription(String description)
    {
        ensureThat(description, equalTo(task.getDescription()));
    }

    @Then("taskexception $name has been thrown")
    public void taskExceptionThrown(String name)
    {
        ensureThat(taskException.getClass().getSimpleName(), CoreMatchers.equalTo(name));

        taskException = null;

    }

    @Then("taskexception is not thrown")
    public void noTaskException()
    {
        ensureThat(taskException, CoreMatchers.nullValue());
    }

    @When("setting the task dueOn")
    public void setDueOn()
    {
        Date future = new Date();
        future.setTime(future.getTime() + 10000);
        task.dueOn(future);
    }

    @Then("the task dueOn $can be found")
    public void dueIsSet(String can)
    {
        TaskEntity taskEntity = (TaskEntity) task;
        if (can.equals("can"))
        {
            ensureThat(taskEntity.dueOn().get(), CoreMatchers.notNullValue());
        } else if (can.equals("cannot"))
        {
            ensureThat(taskEntity.dueOn().get(), CoreMatchers.nullValue());
        } else
        {
            ensureThat(false);
        }

    }

    @When("setting the task dueOn for a date in the past")
    public void setDueOnInPast()
    {
        Date past = new Date();
        past.setTime(past.getTime() - 10000);
        try
        {
            task.dueOn(past);
        } catch (Exception e)
        {
            taskException = e;
        }
    }

    @When("adding a label named $name to the task")
    public void addLabel(String name)
    {
        Label label = userSteps.user.createLabel();
        label.changeDescription(name);

        task.addLabel(label);
    }


    @Then("the task label named $name $can be found")
    public void hasLabel(String name, String can)
    {
        Label label = findTaskLabel(name);

        if (can.equals("can"))
        {
            ensureThat(label, CoreMatchers.notNullValue());
        } else if (can.equals("cannot"))
        {
            ensureThat(label, CoreMatchers.nullValue());
        } else
        {
            ensureThat(false);
        }
    }

    @When("removing task label named $name from task")
    public void removeTaskLabel(String name)
    {
        Label label = findTaskLabel(name);

        TaskEntity taskEntity = (TaskEntity) task;

        taskEntity.removeLabel(label);
    }

    @When("adding note $tasknote")
    public void addTaskNote(String tasknote)
    {
        task.changeNote(tasknote);
    }


    @Then("task has note $tasknote")
    public void hasNote(String tasknote)
    {
        TaskEntity taskEntity = (TaskEntity) task;
        ensureThat(tasknote, CoreMatchers.equalTo(taskEntity.note().get()));
    }

    private Label findTaskLabel(String name)
    {
        TaskEntity taskEntity = (TaskEntity) task;

        for (Label label : taskEntity.labels())
        {
            if (label.getDescription().equals(name))
            {
                return label;
            }
        }

        return null;
    }
}