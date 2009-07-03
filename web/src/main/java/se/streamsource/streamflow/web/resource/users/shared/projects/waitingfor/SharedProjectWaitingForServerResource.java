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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegatable;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.IsRead;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.SharedTaskEntity;
import se.streamsource.streamflow.web.domain.task.TaskStatus;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/waitingfor
 */
public class SharedProjectWaitingForServerResource
        extends CommandQueryServerResource
{
    public WaitingForTaskListDTO tasks()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("project");
        WaitingFor waitingFor = uow.get(WaitingFor.class, id);

        // Find all Active delegated tasks owned by "project"
        // or Completed delegated tasks that are marked as unread
        QueryBuilder<SharedTaskEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedTaskEntity.class);
        Property<String> idProp = templateFor(Ownable.OwnableState.class).owner().get().identity();
        Association<Delegatee> delegatee = templateFor(Delegatable.DelegatableState.class).delegatedTo();
        queryBuilder.where(and(
                eq(idProp, id),
                isNotNull(delegatee),
                or(
                        eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE),
                        and(notEq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE),
                                eq(templateFor(IsRead.IsReadState.class).isRead(), false)))));

        Query<SharedTaskEntity> waitingForQuery = queryBuilder.newQuery();
        waitingForQuery.orderBy(orderBy(templateFor(Delegatable.DelegatableState.class).delegatedOn()));

        ValueBuilder<WaitingForTaskDTO> builder = vbf.newValueBuilder(WaitingForTaskDTO.class);
        WaitingForTaskDTO prototype = builder.prototype();
        ValueBuilder<WaitingForTaskListDTO> listBuilder = vbf.newValueBuilder(WaitingForTaskListDTO.class);
        List<WaitingForTaskDTO> list = listBuilder.prototype().tasks().get();
        for (SharedTaskEntity sharedTask : waitingForQuery)
        {
            Assignee assignee = sharedTask.assignedTo().get();
            if (assignee != null)
                prototype.assignedTo().set(assignee.getDescription());
            prototype.delegatedTo().set(sharedTask.delegatedTo().get().getDescription());
            prototype.task().set(EntityReference.getEntityReference(sharedTask));
            prototype.delegatedOn().set(sharedTask.delegatedOn().get());
            prototype.description().set(sharedTask.description().get());
            prototype.status().set(sharedTask.status().get());
            prototype.isRead().set(sharedTask.isRead().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }
}