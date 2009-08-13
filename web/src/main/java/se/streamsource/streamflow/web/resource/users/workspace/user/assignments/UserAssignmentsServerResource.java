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

package se.streamsource.streamflow.web.resource.users.workspace.user.assignments;

import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.task.NewTaskCommand;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.task.Assignable;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.CreatedOn;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.task.TaskStatus;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/assignments
 */
public class UserAssignmentsServerResource
        extends AbstractTaskListServerResource
{
    public AssignmentsTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Assignments assignments = uow.get(Assignments.class, id);

        // Find all my Active tasks assigned to "me"
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Property<String> assignedId = templateFor(Assignable.AssignableState.class).assignedTo().get().identity();
        Property<String> ownedId = templateFor(Ownable.OwnableState.class).owner().get().identity();
        queryBuilder.where(and(
                eq(assignedId, id),
                eq(ownedId, id),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<TaskEntity> assignmentsQuery = queryBuilder.newQuery(uow);
        assignmentsQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        return buildTaskList(id, assignmentsQuery, AssignedTaskDTO.class, AssignmentsTaskListDTO.class);
    }

    public void newtask(NewTaskCommand command)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Assignments assignments = uow.get(Assignments.class, id);
        Assignee assignee = uow.get(Assignee.class, id);

        Task task = assignments.newAssignedTask(assignee);
        task.describe(command.description().get());
        task.changeNote(command.note().get());
    }
}