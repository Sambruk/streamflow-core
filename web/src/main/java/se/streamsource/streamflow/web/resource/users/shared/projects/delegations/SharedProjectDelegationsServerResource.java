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

package se.streamsource.streamflow.web.resource.users.shared.projects.delegations;

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
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/delegations
 */
public class SharedProjectDelegationsServerResource
        extends CommandQueryServerResource
{
    public DelegationsTaskListDTO tasks()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("project");

        // Find all Active tasks delegated to "project"
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Property<String> idProp = templateFor(Delegatable.DelegatableState.class).delegatedTo().get().identity();
        Association<Owner> ownerProp = templateFor(Ownable.OwnableState.class).owner();
        queryBuilder.where(and(
                eq(idProp, id),
                isNotNull(ownerProp),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<TaskEntity> delegationsQuery = queryBuilder.newQuery(uow);
        delegationsQuery.orderBy(orderBy(templateFor(Delegatable.DelegatableState.class).delegatedOn()));

        ValueBuilder<DelegatedTaskDTO> builder = vbf.newValueBuilder(DelegatedTaskDTO.class);
        DelegatedTaskDTO prototype = builder.prototype();
        ValueBuilder<DelegationsTaskListDTO> listBuilder = vbf.newValueBuilder(DelegationsTaskListDTO.class);
        List<DelegatedTaskDTO> list = listBuilder.prototype().tasks().get();
        for (TaskEntity task : delegationsQuery)
        {
            Owner owner = uow.get(Owner.class, task.owner().get().identity().get());
            prototype.delegatedFrom().set(owner.getDescription());
            prototype.task().set(EntityReference.getEntityReference(task));
            prototype.delegatedOn().set(task.delegatedOn().get());
            prototype.description().set(task.description().get());
            prototype.status().set(task.status().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }
}