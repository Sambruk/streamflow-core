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

package se.streamsource.streamflow.web.resource.users.shared.projects.waitingfor;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.SharedTask;
import se.streamsource.streamflow.web.domain.task.SharedInbox;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/waitingfor/{task}
 */
public class SharedProjectWaitingForTaskServerResource
        extends CommandQueryServerResource
{

    public void complete()
    {
        String id = (String) getRequest().getAttributes().get("user");
        String taskId = (String) getRequest().getAttributes().get("task");
        UnitOfWork uow = uowf.currentUnitOfWork();
        SharedTask task = uow.get(SharedTask.class, taskId);
        Delegations delegations = uow.get(Delegations.class, id);
        delegations.completeTask(task);
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