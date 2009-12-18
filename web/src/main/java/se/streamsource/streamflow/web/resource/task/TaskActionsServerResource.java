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

package se.streamsource.streamflow.web.resource.task;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.entity.EntityReference;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.task.TaskActions;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.domain.tasktype.TaskTypeQueries;
import se.streamsource.streamflow.web.domain.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
public class TaskActionsServerResource
      extends CommandQueryServerResource
{
   public TaskActionsServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }

   // List possible actions
   public TaskActions actions()
   {
      ValueBuilder<TaskActions> builder = vbf.newValueBuilder( TaskActions.class );
      List<String> actions = builder.prototype().actions().get();

      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      UserEntity user = getUser();

      task.addActions( user, actions);

      return builder.newInstance();
   }

   public ListValue possibletasktypes()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TaskTypeQueries task = uow.get( TaskTypeQueries.class, id );

      return task.taskTypes();
   }

   public ListValue possibleprojects()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      TaskEntity task = uow.get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      return task.possibleProjects();
   }

   public ListValue possibleusers()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      TaskEntity task = uow.get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      return task.possibleUsers();
   }

   public ListValue possiblelabels()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TaskLabelsQueries labels = uow.get( TaskLabelsQueries.class, id );

      return labels.possibleLabels();
   }

   // Commands
   public void accept()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Delegations delegations = (Delegations) task.delegatedTo().get();
         delegations.accept( task, user );
      }

   }

   public void label( EntityReferenceDTO reference )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String taskId = (String) getRequest().getAttributes().get( "task" );

      TaskEntity task = uow.get( TaskEntity.class, taskId );
      Label label = uow.get( Label.class, reference.entity().get().identity() );

      task.addLabel( label );
   }

   public void assign()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.assignTo( task, user );
      }
   }

   public void complete()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or WaitingFor
         if (task.isDelegatedBy( user ))
         {
            WaitingFor waitingFor = task.delegatedFrom().get();
            waitingFor.completeWaitingForTask( task, user );

         } else
         {
            Inbox inbox = (Inbox) owner;
            inbox.completeTask( task, user );
         }
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.completeAssignedTask( task );
      }
   }

   public void done()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      User user = getUser();

      Delegations delegations = (Delegations) task.delegatedTo().get();
      delegations.finishDelegatedTask( task, user );
   }

   public void finish()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      WaitingFor waitingFor = task.delegatedFrom().get();
      waitingFor.completeFinishedTask( task );
   }

   public void forward( EntityReferenceDTO entity )
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Inbox toInbox = uowf.currentUnitOfWork().get( Inbox.class, entity.entity().get().identity() );

      Owner owner = task.owner().get();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.forwardTo( task, toInbox );
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.forwardAssignedTaskTo( task, toInbox );
      }
   }

   public void delegate( EntityReferenceDTO entity )
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Delegatee to = uowf.currentUnitOfWork().get( Delegatee.class, entity.entity().get().identity() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.delegateTo( task, to, user );
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.delegateAssignedTaskTo( task, to );
      }
   }

   public void drop()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.dropTask( task, user );
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.dropAssignedTask( task );
      }

   }

   public void redo()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      WaitingFor waitingFor = task.delegatedFrom().get();
      waitingFor.redoFinishedTask( task );
   }

   public void reject()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      if (task.assignedTo().get() == null)
      {
         // Delegations
         Delegations delegations = (Delegations) task.delegatedTo().get();
         delegations.reject( task );
      } else
      {
         Owner owner = task.owner().get();
         Assignments assignments = (Assignments) owner;
         assignments.rejectAssignedTask(task);
      }
   }

   public void tasktype( EntityReferenceDTO dto )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "task" );
      TypedTask task = uow.get( TypedTask.class, id );

      EntityReference entityReference = dto.entity().get();
      if (entityReference != null)
      {
         TaskType taskType = uow.get( TaskType.class, entityReference.identity() );
         task.changeTaskType( taskType );
      } else
         task.changeTaskType( null );
   }

   private UserEntity getUser()
   {
      Subject subject = Subject.getSubject( AccessController.getContext() );
      if (subject == null)
         return null;
      else
      {
         Iterator<Principal> iterator = subject.getPrincipals().iterator();
         if (iterator.hasNext())
         {
            String userName = iterator.next().getName();
            return uowf.currentUnitOfWork().get( UserEntity.class, userName );
         } else
            return null;
      }
   }
}
