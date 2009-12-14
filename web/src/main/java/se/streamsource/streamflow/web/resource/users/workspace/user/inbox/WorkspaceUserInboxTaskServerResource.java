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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Delegator;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/inbox/{task}
 */
public class WorkspaceUserInboxTaskServerResource
      extends CommandQueryServerResource
{
   public void complete()
   {
      String id = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      Inbox inbox = uowf.currentUnitOfWork().get( Inbox.class, id );
      Assignee assignee = uowf.currentUnitOfWork().get( Assignee.class, id );
      inbox.completeTask( task, assignee );
   }

   public void drop()
   {
      String id = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      Inbox inbox = uowf.currentUnitOfWork().get( Inbox.class, id );
      Assignee assignee = uowf.currentUnitOfWork().get( Assignee.class, id );
      inbox.dropTask( task, assignee );
   }

   public void assignToMe()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Inbox inbox = uow.get( Inbox.class, userId );
      Assignee assignee = uow.get( Assignee.class, userId );
      inbox.assignTo( task, assignee );
   }

   public void delegate( EntityReferenceDTO reference )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Inbox inbox = uow.get( Inbox.class, userId );
      Delegator delegator = uow.get( Delegator.class, userId );
      Delegatee delegatee = uow.get( Delegatee.class, reference.entity().get().identity() );
      inbox.delegateTo( task, delegatee, delegator ); // TODO Role
   }

   public void forward( EntityReferenceDTO reference )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      TaskEntity task = uow.get( TaskEntity.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Inbox inbox = uow.get( Inbox.class, userId );
      Inbox receiverInbox = uow.get( Inbox.class, reference.entity().get().identity() );
      inbox.forwardTo( task, receiverInbox );
   }

   public void markAsRead()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Inbox inbox = uow.get( Inbox.class, userId );
      inbox.markAsRead( task );
   }

   public void markAsUnread()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Inbox inbox = uow.get( Inbox.class, userId );
      inbox.markAsUnread( task );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String userId = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Inbox inbox = uow.get( Inbox.class, userId );
      TaskEntity task = uow.get( TaskEntity.class, taskId );

      inbox.deleteTask( task );
   }
}