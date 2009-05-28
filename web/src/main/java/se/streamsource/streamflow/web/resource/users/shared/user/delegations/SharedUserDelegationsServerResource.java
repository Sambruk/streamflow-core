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

package se.streamsource.streamflow.web.resource.users.shared.user.delegations;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskValue;
import se.streamsource.streamflow.resource.delegation.DelegationsTaskListValue;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.web.domain.task.CreatedOn;
import se.streamsource.streamflow.web.domain.task.Delegatable;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.SharedTaskEntity;
import se.streamsource.streamflow.web.domain.task.TaskStatus;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/user/delegations
 */
public class SharedUserDelegationsServerResource
        extends CommandQueryServerResource
{
    public DelegationsTaskListValue tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Delegations delegations = uow.get(Delegations.class, id);

        // Find all Active tasks delegated to "me"
        QueryBuilder<SharedTaskEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedTaskEntity.class);
        Property<String> idProp = templateFor(Delegatable.DelegatableState.class).delegatedTo().get().identity();
        queryBuilder.where(and(
                eq(idProp, id),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<SharedTaskEntity> assignmentsQuery = queryBuilder.newQuery();
        assignmentsQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        ValueBuilder<DelegatedTaskValue> builder = vbf.newValueBuilder(DelegatedTaskValue.class);
        DelegatedTaskValue prototype = builder.prototype();
        ValueBuilder<DelegationsTaskListValue> listBuilder = vbf.newValueBuilder(DelegationsTaskListValue.class);
        List<DelegatedTaskValue> list = listBuilder.prototype().tasks().get();
        EntityReference ref = EntityReference.parseEntityReference(id);
        for (SharedTaskEntity sharedTask : assignmentsQuery)
        {
            prototype.owner().set(ref);
            prototype.task().set(EntityReference.getEntityReference(sharedTask));
            prototype.creationDate().set(sharedTask.createdOn().get());
            prototype.description().set(sharedTask.description().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }
}