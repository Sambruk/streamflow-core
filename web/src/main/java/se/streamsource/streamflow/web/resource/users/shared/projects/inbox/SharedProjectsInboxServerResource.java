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
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/inbox
 */
public class SharedProjectsInboxServerResource
        extends CommandQueryServerResource
{
    public InboxTaskListDTO tasks(TasksQuery query)
    {
        //todo check if user is participanting in project
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("project");

        // Find all Active tasks with specific owner which have not yet been assigned
        QueryBuilder<SharedTaskEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedTaskEntity.class);
        Property<String> ownableId = templateFor(Ownable.OwnableState.class).owner().get().identity();
        Association<Assignee> assignee = templateFor(Assignable.AssignableState.class).assignedTo();
        Association<Delegatee> delegatee = templateFor(Delegatable.DelegatableState.class).delegatedTo();
        queryBuilder.where(and(
                eq(ownableId, id),
                isNull(assignee),
                isNull(delegatee),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<SharedTaskEntity> inboxQuery = queryBuilder.newQuery();
        inboxQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        ValueBuilder<InboxTaskDTO> builder = vbf.newValueBuilder(InboxTaskDTO.class);
        InboxTaskDTO prototype = builder.prototype();
        ValueBuilder<InboxTaskListDTO> listBuilder = vbf.newValueBuilder(InboxTaskListDTO.class);
        List<InboxTaskDTO> list = listBuilder.prototype().tasks().get();
        EntityReference ref = EntityReference.parseEntityReference(id);
        for (SharedTaskEntity sharedTask : inboxQuery)
        {
            prototype.owner().set(ref);
            prototype.task().set(EntityReference.getEntityReference(sharedTask));
            prototype.creationDate().set(sharedTask.createdOn().get());
            prototype.description().set(sharedTask.description().get());
            prototype.status().set(sharedTask.status().get());
            prototype.isRead().set(sharedTask.isRead().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }

    public void newtask(NewSharedTaskCommand command)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("project");
        UserEntity user = uow.get(UserEntity.class, id);

        EntityBuilder<SharedTaskEntity> builder = uow.newEntityBuilder(SharedTaskEntity.class);
        SharedTaskEntity prototype = builder.prototype();
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
            for (SharedTask sharedTask : path.getPath())
            {
                prototype.path().add(prototype.path().count(), sharedTask);
            }
            prototype.path().add(prototype.path().count(), (SharedTask) path);
        }

        SharedTaskEntity task = builder.newInstance();
        user.receiveTask(task);
    }
}