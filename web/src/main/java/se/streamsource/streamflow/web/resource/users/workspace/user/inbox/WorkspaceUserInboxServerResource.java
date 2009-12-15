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

package se.streamsource.streamflow.web.resource.users.workspace.user.inbox;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.group.Participation;
import se.streamsource.streamflow.web.domain.task.InboxQueries;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/inbox
 */
public class WorkspaceUserInboxServerResource
      extends AbstractTaskListServerResource
{
   @Structure
   ValueBuilderFactory vbf;

   public TaskListDTO tasks( TasksQuery query )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "user" );
      InboxQueries inbox = uow.get( InboxQueries.class, id );

      return inbox.inboxTasks();
   }

   public void createtask()
   {
      String userId = (String) getRequest().getAttributes().get( "user" );

      createTask( userId );
   }

   public ListValue projects()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String userId = (String) getRequest().getAttributes().get( "user" );
      Participation.Data user = uow.get( Participation.Data.class, userId );
      ListValueBuilder builder = new ListValueBuilder( vbf );

      return builder.addDescribableItems( user.allProjects() ).newList();
   }
}
