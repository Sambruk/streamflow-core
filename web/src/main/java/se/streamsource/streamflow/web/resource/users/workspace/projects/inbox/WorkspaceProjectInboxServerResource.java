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

package se.streamsource.streamflow.web.resource.users.workspace.projects.inbox;

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.task.InboxQueries;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/projects/{project}/inbox
 */
public class WorkspaceProjectInboxServerResource
      extends AbstractTaskListServerResource
{
   public InboxTaskListDTO tasks( TasksQuery query )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "project" );
      InboxQueries inbox = uow.get( InboxQueries.class, id );

      return inbox.inboxTasks();
   }

   public void createtask()
   {
      String projectId = (String) getRequest().getAttributes().get( "project" );

      createTask( projectId );
   }
}
