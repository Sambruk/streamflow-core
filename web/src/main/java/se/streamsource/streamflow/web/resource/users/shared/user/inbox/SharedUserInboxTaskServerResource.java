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

package se.streamsource.streamflow.web.resource.users.shared.user.inbox;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.SharedInbox;
import se.streamsource.streamflow.web.domain.task.SharedTask;
import se.streamsource.streamflow.web.domain.task.SharedTaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/shared/user/inbox/{task}
 */
public class SharedUserInboxTaskServerResource
        extends CommandQueryServerResource
{
    public void complete()
    {
        String id = (String) getRequest().getAttributes().get("user");
        String taskId = (String) getRequest().getAttributes().get("task");
        SharedTask task = uowf.currentUnitOfWork().get(SharedTask.class, taskId);
        SharedInbox inbox = uowf.currentUnitOfWork().get(SharedInbox.class, id);
        inbox.completeTask(task);
    }

    public void describe(DescriptionValue descriptionValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.describe(descriptionValue.description().get());
    }

    public void assignToMe()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTask task = uow.get(SharedTask.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, userId);
        inbox.assignToMe(task);
    }

    public void delegate(EntityReferenceValue reference)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTask task = uow.get(SharedTask.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, userId);
        Delegatee delegatee = uow.get(Delegatee.class, reference.entity().get().identity());
        inbox.delegate(task, delegatee);
    }

    public void forward(EntityReferenceValue reference)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTaskEntity task = uow.get(SharedTaskEntity.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, userId);
        SharedInbox receiverInbox = uow.get(SharedInbox.class, reference.entity().get().identity());
        receiverInbox.receiveTask(task);
        //inbox.remove();
    }

    public void markAsRead()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTask task = uow.get(SharedTask.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, userId);
        inbox.markAsRead(task);
    }

    public void markAsUnRead()
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTask task = uow.get(SharedTask.class, taskId);
        String userId = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, userId);
        inbox.markAsUnread(task);
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException
    {
        try
        {
            String taskId = (String) getRequest().getAttributes().get("task");
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete task"));
            SharedTask sharedTask = uow.get(SharedTask.class, taskId);
            uow.remove(sharedTask);
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}