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

package se.streamsource.streamflow.web.resource.users.shared.user.inbox.task;

import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.web.domain.task.SharedInbox;
import se.streamsource.streamflow.web.domain.task.SharedTask;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/shared/user/inbox/{task}
 */
public class SharedUserTaskServerResource
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

    public void describe(DescriptionValue descriptionValue) throws ResourceException
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.describe(descriptionValue.description().get());
    }

    @Override
    protected Representation delete() throws ResourceException
    {
        return super.delete();
    }
}