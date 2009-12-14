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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/assignments/{task}
 */
public class WorkspaceUserAssignedTaskServerResource
      extends CommandQueryServerResource
{
   public void complete()
   {
      String userId = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      Assignments assignments = uowf.currentUnitOfWork().get( Assignments.class, userId );
      Assignee assignee = uowf.currentUnitOfWork().get( Assignee.class, userId );
      assignments.completeAssignedTask( task );
   }

   public void changedescription( StringDTO stringValue )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Describable describable = uowf.currentUnitOfWork().get( Describable.class, taskId );
      describable.changeDescription( stringValue.string().get() );
   }

   public void drop()
   {
      String id = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      Assignments assignments = uowf.currentUnitOfWork().get( Assignments.class, id );
      assignments.dropAssignedTask( task );
   }

   public void delegate( EntityReferenceDTO reference )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Assignments assignments = uowf.currentUnitOfWork().get( Assignments.class, userId );
      Delegatee delegatee = uow.get( Delegatee.class, reference.entity().get().identity() );
      assignments.delegateAssignedTaskTo( task, delegatee );
   }

   public void forward( EntityReferenceDTO reference )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String userId = (String) getRequest().getAttributes().get( "user" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      TaskEntity task = uow.get( TaskEntity.class, taskId );
      Inbox receiverInbox = uow.get( Inbox.class, reference.entity().get().identity() );

      Assignments assignments = uowf.currentUnitOfWork().get( Assignments.class, userId );
      assignments.forwardAssignedTaskTo( task, receiverInbox );
   }

   public void markAsRead()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Assignments assignments = uow.get( Assignments.class, userId );
      assignments.markAssignedTaskAsRead( task );
   }

   public void markAsUnread()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "user" );
      Assignments assignments = uow.get( Assignments.class, userId );
      assignments.markAssignedTaskAsUnread( task );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String userId = (String) getRequest().getAttributes().get( "user" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Assignments assignments = uow.get( Assignments.class, userId );
      TaskEntity task = uow.get( TaskEntity.class, taskId );

      assignments.deleteAssignedTask( task );
   }
}