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
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.inbox.InboxTaskListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskValue;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.web.domain.task.CreatedOn;
import se.streamsource.streamflow.web.domain.task.OwnableTask;
import se.streamsource.streamflow.web.domain.task.SharedInbox;
import se.streamsource.streamflow.web.domain.task.SharedTaskEntity;
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
    public InboxTaskListValue tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        SharedInbox inbox = uow.get(SharedInbox.class, id);

        // Find all Active tasks with specific owner
        QueryBuilder<SharedTaskEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedTaskEntity.class);
        Property<String> idProp = QueryExpressions.templateFor(OwnableTask.OwnableTaskState.class).owner().get().identity();
        queryBuilder.where(QueryExpressions.and(
                QueryExpressions.eq(idProp, id),
                QueryExpressions.eq(QueryExpressions.templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<SharedTaskEntity> inboxQuery = queryBuilder.newQuery();
        inboxQuery.orderBy(QueryExpressions.orderBy(QueryExpressions.templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        ValueBuilder<InboxTaskValue> builder = vbf.newValueBuilder(InboxTaskValue.class);
        InboxTaskValue prototype = builder.prototype();
        ValueBuilder<InboxTaskListValue> listBuilder = vbf.newValueBuilder(InboxTaskListValue.class);
        List<InboxTaskValue> list = listBuilder.prototype().tasks().get();
        EntityReference ref = EntityReference.parseEntityReference(id);
        for (SharedTaskEntity sharedTask : inboxQuery)
        {
            prototype.owner().set(ref);
            prototype.task().set(EntityReference.getEntityReference(sharedTask));
            prototype.creationDate().set(sharedTask.createdOn().get());
            prototype.description().set(sharedTask.description().get());
            prototype.status().set(sharedTask.status().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }

    public void newtask(NewSharedTaskCommand command)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        UserEntity user = uow.get(UserEntity.class, id);

        EntityBuilder<SharedTaskEntity> builder = uow.newEntityBuilder(SharedTaskEntity.class);
        builder.prototype().description().set(command.description().get());
        builder.prototype().note().set(command.note().get());
        SharedTaskEntity task = builder.newInstance();
        user.receiveTask(task);
    }
}
