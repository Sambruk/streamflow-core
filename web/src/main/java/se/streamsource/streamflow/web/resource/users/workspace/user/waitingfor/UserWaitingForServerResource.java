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

package se.streamsource.streamflow.web.resource.users.workspace.user.waitingfor;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/waitingfor
 */
public class UserWaitingForServerResource
        extends AbstractTaskListServerResource
{
    public WaitingForTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String userId = (String) getRequest().getAttributes().get("user");
        WaitingFor delegations = uow.get(WaitingFor.class, userId);


        // Find all Active delegated tasks delegated by "me"
        // or Completed delegated tasks that are marked as unread
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Association<WaitingFor> delegatedFrom = templateFor(Delegatable.DelegatableState.class).delegatedFrom();
        Query<TaskEntity> waitingForQuery = queryBuilder.where(and(
                eq(delegatedFrom, delegations),
                or(
                        eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE),
                        eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.DONE)))).
                newQuery(uow);
        waitingForQuery.orderBy(orderBy(templateFor(Delegatable.DelegatableState.class).delegatedOn()));

        return buildTaskList(waitingForQuery, WaitingForTaskDTO.class, WaitingForTaskListDTO.class);
    }

    @Override
    protected <T extends TaskListDTO> void buildTask(TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task)
    {
        WaitingForTaskDTO taskDTO = (WaitingForTaskDTO) prototype;
        Assignee assignee = task.assignedTo().get();
        if (assignee != null)
            taskDTO.assignedTo().set(assignee.getDescription());
        taskDTO.delegatedTo().set(task.delegatedTo().get().getDescription());
        taskDTO.delegatedOn().set(task.delegatedOn().get());
        prototype.isRead().set(true);
        super.buildTask(prototype, labelBuilder, labelPrototype, task);
    }


    public void reject(EntityReferenceDTO taskRef)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String userId = (String) getRequest().getAttributes().get("user");
        WaitingFor delegations = uow.get(WaitingFor.class, userId);

        Task task = uow.get(Task.class, taskRef.entity().get().identity());

        delegations.rejectFinishedTask(task);
    }


    public void completeFinishedTask(EntityReferenceDTO taskRef)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String userId = (String) getRequest().getAttributes().get("user");
        WaitingFor delegations = uow.get(WaitingFor.class, userId);

        Task task = uow.get(Task.class, taskRef.entity().get().identity());

        delegations.completeFinishedTask(task);
    }


   public void completeWaitingForTask(EntityReferenceDTO taskRef)
   {
       UnitOfWork uow = uowf.currentUnitOfWork();
       String userId = (String) getRequest().getAttributes().get("user");
       WaitingFor delegations = uow.get(WaitingFor.class, userId);
       Assignee assignee = uow.get(Assignee.class, userId);

       Task task = uow.get(Task.class, taskRef.entity().get().identity());

       delegations.completeWaitingForTask(task, assignee);
   }
}
