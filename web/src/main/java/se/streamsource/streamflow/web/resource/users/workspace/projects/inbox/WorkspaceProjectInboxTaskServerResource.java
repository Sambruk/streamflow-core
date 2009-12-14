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
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Delegator;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/projects/{project}/inbox/{task}
 */
public class WorkspaceProjectInboxTaskServerResource
      extends CommandQueryServerResource
{
   public void complete()
   {
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      Inbox inbox = uowf.currentUnitOfWork().get( Inbox.class, projectId );
      Assignee assignee = uowf.currentUnitOfWork().get( Assignee.class, userId );
      inbox.completeTask( task, assignee );
   }

   public void changedescription( StringDTO stringValue )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Describable describable = uowf.currentUnitOfWork().get( Describable.class, taskId );
      describable.changeDescription( stringValue.string().get() );
   }

   public void assignToMe()
   {
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      Assignee assignee = uow.get( Assignee.class, userId );
      Inbox inbox = uow.get( Inbox.class, projectId );
      inbox.assignTo( task, assignee );
   }

   public void delegate( EntityReferenceDTO reference )
   {
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      Inbox inbox = uow.get( Inbox.class, projectId );
      Delegator delegator = uow.get( Delegator.class, userId );
      Delegatee delegatee = uow.get( Delegatee.class, reference.entity().get().identity() );
      inbox.delegateTo( task, delegatee, delegator );
   }

   public void forward( EntityReferenceDTO reference )
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      TaskEntity task = uow.get( TaskEntity.class, taskId );
      Inbox receiverInbox = uow.get( Inbox.class, reference.entity().get().identity() );
      receiverInbox.receiveTask( task );
   }

   public void markAsRead()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "project" );
      Inbox inbox = uow.get( Inbox.class, userId );
      inbox.markAsRead( task );
   }

   public void markAsUnread()
   {
      String taskId = (String) getRequest().getAttributes().get( "task" );
      UnitOfWork uow = uowf.currentUnitOfWork();
      Task task = uow.get( Task.class, taskId );
      String userId = (String) getRequest().getAttributes().get( "project" );
      Inbox inbox = uow.get( Inbox.class, userId );
      inbox.markAsUnread( task );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Inbox inbox = uow.get( Inbox.class, projectId );
      TaskEntity task = uow.get( TaskEntity.class, taskId );

      inbox.deleteTask( task );
   }
}