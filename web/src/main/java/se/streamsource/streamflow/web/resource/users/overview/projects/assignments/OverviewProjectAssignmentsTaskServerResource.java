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

package se.streamsource.streamflow.web.resource.users.overview.projects.assignments;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/overview/projects/{project}/assignments/{task}
 */
public class OverviewProjectAssignmentsTaskServerResource
        extends CommandQueryServerResource
{
    public void complete()
    {
        String userId = (String) getRequest().getAttributes().get("user");
        String projectId = (String) getRequest().getAttributes().get("project");
        String taskId = (String) getRequest().getAttributes().get("task");
        Task task = uowf.currentUnitOfWork().get(Task.class, taskId);
        Inbox inbox = uowf.currentUnitOfWork().get(Inbox.class, projectId);
        Assignee assignee = uowf.currentUnitOfWork().get(Assignee.class, userId);
        inbox.completeTask(task, assignee);
    }

    public void describe(StringDTO stringValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.describe(stringValue.string().get());
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String userId = (String) getRequest().getAttributes().get("user");
        String taskId = (String) getRequest().getAttributes().get("task");
        Owner owner = uow.get(Owner.class, userId);
        TaskEntity task = uow.get(TaskEntity.class, taskId);

        if (task.owner().get().equals(owner))
        {
            // Only delete task if user owns it
            uow.remove(task);
        }
    }
}