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

package se.streamsource.streamflow.web.resource.users.workspace.projects.delegations;

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/projects/{project}/delegations/{task}
 */
public class WorkspaceProjectDelegationsTaskServerResource
        extends CommandQueryServerResource
{
    public void complete()
    {
        String projectId = (String) getRequest().getAttributes().get("project");
        String userId = (String) getRequest().getAttributes().get("user");
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        Delegations delegations = uow.get(Delegations.class, projectId);
        Assignee assignee = uow.get(Assignee.class, userId);
        delegations.finishDelegatedTask(task, assignee);
    }

    public void assignToMe()
    {
        String projectId = (String) getRequest().getAttributes().get("project");
        String userId = (String) getRequest().getAttributes().get("user");
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Delegations delegations = uow.get(Delegations.class, projectId);
        Assignee assignee = uow.get(Assignee.class, userId);
        Task task = uow.get(Task.class, taskId);

        delegations.accept(task, assignee);
    }

    public void reject()
    {
        String projectId = (String) getRequest().getAttributes().get("project");
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        Delegations user = uow.get(Delegations.class, projectId);
        user.reject(task);
    }
}