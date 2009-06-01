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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.delegation.DelegationsTaskListDTO;
import se.streamsource.streamflow.web.domain.task.Assignable;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegatable;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.Owner;
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
    public DelegationsTaskListDTO tasks()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Delegations delegations = uow.get(Delegations.class, id);

        // Find all Active tasks delegated to "me"
        QueryBuilder<SharedTaskEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedTaskEntity.class);
        Property<String> idProp = templateFor(Delegatable.DelegatableState.class).delegatedTo().get().identity();
        Association<Assignee> assignee = templateFor(Assignable.AssignableState.class).assignedTo();
        queryBuilder.where(and(
                eq(idProp, id),
                isNull(assignee),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<SharedTaskEntity> delegationsQuery = queryBuilder.newQuery();
        delegationsQuery.orderBy(orderBy(templateFor(Delegatable.DelegatableState.class).delegatedOn()));

        ValueBuilder<DelegatedTaskDTO> builder = vbf.newValueBuilder(DelegatedTaskDTO.class);
        DelegatedTaskDTO prototype = builder.prototype();
        ValueBuilder<DelegationsTaskListDTO> listBuilder = vbf.newValueBuilder(DelegationsTaskListDTO.class);
        List<DelegatedTaskDTO> list = listBuilder.prototype().tasks().get();
        for (SharedTaskEntity sharedTask : delegationsQuery)
        {
            Owner owner = uow.get(Owner.class, sharedTask.owner().get().identity().get());
            prototype.delegatedFrom().set(owner.getDescription());
            prototype.task().set(EntityReference.getEntityReference(sharedTask));
            prototype.delegatedOn().set(sharedTask.delegatedOn().get());
            prototype.description().set(sharedTask.description().get());
            prototype.status().set(sharedTask.status().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }
}