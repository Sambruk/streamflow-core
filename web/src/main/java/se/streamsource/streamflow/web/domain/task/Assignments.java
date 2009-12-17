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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import static se.streamsource.streamflow.domain.task.TaskStates.*;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.user.User;

/**
 * JAVADOC
 */
@Mixins(Assignments.Mixin.class)
public interface Assignments
{
   Task createAssignedTask( Assignee assignee );

   void completeAssignedTask( @HasStatus(ACTIVE) Task task );

   void dropAssignedTask( @HasStatus(ACTIVE) Task task );

   void delegateAssignedTaskTo( @HasStatus(ACTIVE) Task task, Delegatee delegatee );

   void forwardAssignedTaskTo( @HasStatus(ACTIVE) Task task, Inbox receiverInbox );

   void deleteAssignedTask( @HasStatus(ACTIVE) Task task );

   void rejectAssignedTask( @HasStatus(ACTIVE) Task task );

   interface Data
   {
      Task createdAssignedTask( DomainEvent event, String id );

      void deletedAssignedTask( DomainEvent event, Task task );
   }


   abstract class Mixin
         implements Assignments, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      @This
      WaitingFor waitingFor;

      @Service
      IdentityGenerator idGenerator;

      public Task createAssignedTask( Assignee assignee )
      {
         TaskEntity taskEntity = (TaskEntity) createdAssignedTask( DomainEvent.CREATE, idGenerator.generate( TaskEntity.class ) );
         taskEntity.changeOwner( owner );
         taskEntity.addContact( vbf.newValue( ContactValue.class ) );
         taskEntity.assignTo( assignee );

         return taskEntity;
      }

      public Task createdAssignedTask( DomainEvent event, String id )
      {
         EntityBuilder<TaskEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( TaskEntity.class, id );
         builder.instance().createdOn().set( event.on().get() );
         try
         {
            User user = uowf.currentUnitOfWork().get( User.class, event.by().get() );
            builder.instance().createdBy().set( user );
         } catch (NoSuchEntityException e)
         {
            // Ignore
         }
         return builder.newInstance();
      }

      public void completeAssignedTask( Task task )
      {
         // Complete (TaskStates.COMPLETED) assigned tasks
         // but finish (TaskStates.DONE) assigned AND delegated
         // tasks.
         if (((Delegatable.Data) task).delegatedFrom().get() != null)
         {
            task.done();
         } else
         {
            task.complete();
         }
      }

      public void dropAssignedTask( Task task )
      {
         task.drop();
      }

      public void delegateAssignedTaskTo( Task task, Delegatee delegatee )
      {
         Assignable.Data assignable = (Assignable.Data) task;
         Delegator delegator = (Delegator) assignable.assignedTo().get();
         task.unassign();
         task.delegateTo( delegatee, delegator, waitingFor );
      }

      public void forwardAssignedTaskTo( Task task, Inbox receiverInbox )
      {
         task.unassign();
         receiverInbox.receiveTask( task );
      }

      public void deleteAssignedTask( Task task )
      {
         deletedAssignedTask( DomainEvent.CREATE, task );
      }

      public void rejectAssignedTask( @HasStatus(ACTIVE) Task task )
      {
         task.rejectDelegation();
      }

      public void deletedAssignedTask( DomainEvent event, Task task )
      {
         uowf.currentUnitOfWork().remove( task );
      }
   }
}
