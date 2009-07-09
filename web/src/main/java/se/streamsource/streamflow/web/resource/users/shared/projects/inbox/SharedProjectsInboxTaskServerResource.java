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

package se.streamsource.streamflow.web.resource.users.shared.projects.inbox;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/inbox/{task}
 */
public class SharedProjectsInboxTaskServerResource
        extends CommandQueryServerResource
{
    public void complete()
    {
        String id = (String) getRequest().getAttributes().get("project");
        String taskId = (String) getRequest().getAttributes().get("task");
        Task task = uowf.currentUnitOfWork().get(Task.class, taskId);
        Inbox inbox = uowf.currentUnitOfWork().get(Inbox.class, id);
        inbox.completeTask(task);
    }

    public void describe(DescriptionDTO descriptionValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.describe(descriptionValue.description().get());
    }

    public void assignToMe()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        Inbox inbox = uow.get(Inbox.class, userId);
        inbox.assignToMe(task);
    }

    public void delegate(EntityReferenceDTO reference)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        Inbox inbox = uow.get(Inbox.class, userId);
        Delegatee delegatee = uow.get(Delegatee.class, reference.entity().get().identity());
        inbox.delegate(task, delegatee);
    }

    public void forward(EntityReferenceDTO reference)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        TaskEntity task = uow.get(TaskEntity.class, taskId);
        Inbox receiverInbox = uow.get(Inbox.class, reference.entity().get().identity());
        receiverInbox.receiveTask(task);
        receiverInbox.markAsUnread(task);
    }

    public void markAsRead()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        Inbox inbox = uow.get(Inbox.class, userId);
        inbox.markAsRead(task);
    }

    public void markAsUnRead()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        Task task = uow.get(Task.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        Inbox inbox = uow.get(Inbox.class, userId);
        inbox.markAsUnread(task);
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException
    {
        try
        {
            String taskId = (String) getRequest().getAttributes().get("task");
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete task"));
            Task task = uow.get(Task.class, taskId);
            uow.remove(task);
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}