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
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.task.TaskActions;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;
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

      if (task.assignedTo().get() == null)
      {
         if (task.delegatedTo().get() == null)
         {
            // Inbox
            if (task.status().get().equals( TaskStates.ACTIVE ))
               actions.add( "complete" );

            actions.add("assign");
            actions.add("forward");
            actions.add("delegate");
            actions.add("drop");
            actions.add("delete");
         } else
         {
            // Delegations/WaitingFor
         }
      } else
      {
         if (task.isAssignedTo(user))
         {
            // Assignments

            if (task.status().get().equals( TaskStates.ACTIVE ))
               actions.add( "complete" );
         }
      }

      return builder.newInstance();
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
   
   // Commands
   public void complete()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.completeTask( task, user );
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.completeAssignedTask( task );
      }
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

   public void forward( EntityReferenceDTO entity)
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Inbox toInbox = uowf.currentUnitOfWork().get( Inbox.class, entity.entity().get().identity() );

      Owner owner = task.owner().get();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.forwardTo( task,  toInbox);
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.forwardAssignedTaskTo( task,  toInbox);
      }
   }

   public void delegate( EntityReferenceDTO entity)
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Delegatee to = uowf.currentUnitOfWork().get( Delegatee.class, entity.entity().get().identity() );

      Owner owner = task.owner().get();

      User user = getUser();

      if (task.assignedTo().get() == null)
      {
         // Inbox or Delegations/WaitingFor
         Inbox inbox = (Inbox) owner;
         inbox.delegateTo( task,  to, user);
      } else
      {
         Assignments assignments = (Assignments) owner;
         assignments.delegateAssignedTaskTo( task,  to);
      }
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
