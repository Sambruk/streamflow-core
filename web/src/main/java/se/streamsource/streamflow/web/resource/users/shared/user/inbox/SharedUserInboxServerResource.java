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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.web.domain.task.Assignable;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.CreatedOn;
import se.streamsource.streamflow.web.domain.task.Delegatable;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.task.TaskPath;
import se.streamsource.streamflow.web.domain.task.TaskStatus;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/user/inbox
 */
public class SharedUserInboxServerResource
        extends CommandQueryServerResource
{
    public InboxTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Inbox inbox = uow.get(Inbox.class, id);

        // Find all Active tasks with specific owner which have not yet been assigned
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Property<String> ownableId = templateFor(Ownable.OwnableState.class).owner().get().identity();
        Association<Assignee> assignee = templateFor(Assignable.AssignableState.class).assignedTo();
        Association<Delegatee> delegatee = templateFor(Delegatable.DelegatableState.class).delegatedTo();
        queryBuilder.where(and(
                eq(ownableId, id),
                isNull(assignee),
                isNull(delegatee),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<TaskEntity> inboxQuery = queryBuilder.newQuery(uow);
        inboxQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        ValueBuilder<InboxTaskDTO> builder = vbf.newValueBuilder(InboxTaskDTO.class);
        InboxTaskDTO prototype = builder.prototype();
        ValueBuilder<InboxTaskListDTO> listBuilder = vbf.newValueBuilder(InboxTaskListDTO.class);
        List<InboxTaskDTO> list = listBuilder.prototype().tasks().get();
        EntityReference ref = EntityReference.parseEntityReference(id);
        for (TaskEntity task : inboxQuery)
        {
            prototype.owner().set(ref);
            prototype.task().set(EntityReference.getEntityReference(task));
            prototype.creationDate().set(task.createdOn().get());
            prototype.description().set(task.description().get());
            prototype.status().set(task.status().get());
            prototype.isRead().set(task.isRead().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }

    public void newtask(NewSharedTaskCommand command)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        UserEntity user = uow.get(UserEntity.class, id);

        EntityBuilder<TaskEntity> builder = uow.newEntityBuilder(TaskEntity.class);
        TaskEntity prototype = builder.prototype();
        prototype.description().set(command.description().get());
        prototype.note().set(command.note().get());
        if (command.isCompleted().get())
        {
            prototype.status().set(TaskStates.COMPLETED);
        }

        // Check if subtask
        if (command.parentTask().get() != null)
        {
            TaskPath path = uow.get(TaskPath.class, command.parentTask().get().identity());

            // Add parents path first, then parent itself
            for (Task task : path.getPath())
            {
                prototype.path().add(prototype.path().count(), task);
            }
            prototype.path().add(prototype.path().count(), (Task) path);
        }

        TaskEntity task = builder.newInstance();
        user.receiveTask(task);
    }
}
