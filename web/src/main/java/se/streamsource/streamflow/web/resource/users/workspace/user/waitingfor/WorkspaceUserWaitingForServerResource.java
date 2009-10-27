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

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegator;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.domain.task.WaitingForQueries;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/waitingfor
 */
public class WorkspaceUserWaitingForServerResource
        extends AbstractTaskListServerResource
{
    public WaitingForTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String userId = (String) getRequest().getAttributes().get("user");
        Delegator delegator = uow.get( Delegator.class, userId );
        WaitingForQueries waitingFor = uow.get(WaitingForQueries.class, userId);

        return waitingFor.waitingForTasks( delegator );
    }
}
