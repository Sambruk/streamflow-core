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

package se.streamsource.streamflow.web.resource.users.workspace.projects.waitingfor;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/projects/{project}/waitingfor/{task}
 */
public class WorkspaceProjectWaitingForTaskServerResource
      extends CommandQueryServerResource
{
   public void assignToMe()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );

      WaitingFor waitingFor = uow.get( WaitingFor.class, projectId );
      Assignee assignee = uow.get( Assignee.class, userId );
      Task task = uow.get( Task.class, taskId );

      waitingFor.assignWaitingForTask( task, assignee );
   }

   public void markAsRead()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String projectId = (String) getRequest().getAttributes().get( "project" );

      Task task = uow.get( Task.class, taskId );
      WaitingFor waitingFor = uow.get( WaitingFor.class, projectId );

      waitingFor.markWaitingForAsRead( task );
   }

   public void reject()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String projectId = (String) getRequest().getAttributes().get( "project" );

      WaitingFor waitingFor = uow.get( WaitingFor.class, projectId );
      Task task = uow.get( Task.class, taskId );

      waitingFor.rejectFinishedTask( task );
   }

   public void completeFinishedTask()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String projectId = (String) getRequest().getAttributes().get( "project" );

      WaitingFor waitingFor = uow.get( WaitingFor.class, projectId );
      Task task = uow.get( Task.class, taskId );

      waitingFor.completeFinishedTask( task );
   }


   public void completeWaitingForTask()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );

      WaitingFor delegations = uow.get( WaitingFor.class, projectId );
      Assignee assignee = uow.get( Assignee.class, userId );
      Task task = uow.get( Task.class, taskId );

      delegations.completeWaitingForTask( task, assignee );
   }

   public void drop()
   {
      String userId = (String) getRequest().getAttributes().get( "user" );
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      Task task = uowf.currentUnitOfWork().get( Task.class, taskId );
      WaitingFor waitingFor = uowf.currentUnitOfWork().get( WaitingFor.class, projectId );
      Assignee assignee = uowf.currentUnitOfWork().get( Assignee.class, userId );
      waitingFor.dropWaitingForTask( task, assignee );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String projectId = (String) getRequest().getAttributes().get( "project" );
      String taskId = (String) getRequest().getAttributes().get( "task" );
      WaitingFor waitingFor = uow.get( WaitingFor.class, projectId );
      TaskEntity task = uow.get( TaskEntity.class, taskId );

      waitingFor.deleteWaitingForTask( task );
   }
}