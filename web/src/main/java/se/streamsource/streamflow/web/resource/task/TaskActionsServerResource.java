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

import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
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
   public Actions actions()
   {
      ValueBuilder<Actions> builder = vbf.newValueBuilder( se.streamsource.streamflow.domain.interaction.gtd.Actions.class );
      List<String> actions = builder.prototype().actions().get();

      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      Actor actor = getActor();

      task.addActions( actor, actions);

      return builder.newInstance();
   }

/*
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
*/

   // Commands
   public void accept()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Actor actor = getActor();

      if (!task.isAssigned())
      {
         // Delegations
         Owner owner = (Owner) task.delegatedTo().get();
         task.assignTo( actor );
         task.sendTo( owner );
      }
   }

   public void assign()
   {
      Assignable task = uowf.currentUnitOfWork().get( Assignable.class, getRequest().getAttributes().get( "task" ).toString() );

      Assignee assignee = getActor();

      if (!task.isAssigned())
      {
         task.assignTo( assignee );
      }
   }

   public void complete()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner owner = task.owner().get();

      Actor actor = getActor();

      if (!task.isAssigned())
      {
         // Inbox or WaitingFor
         if (task.isDelegatedBy( actor ))
         {
            task.sendTo( owner );

         }

         task.assignTo( actor );
      }

      task.complete();
   }

   public void done()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );
      Actor actor = getActor();

      if (!task.isAssigned())
         task.assignTo( actor );
      task.sendTo( (Owner) task.delegatedTo().get() );
      task.done();
   }

   public void sendto( EntityReferenceDTO entity )
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Owner toOwner = uowf.currentUnitOfWork().get( Owner.class, entity.entity().get().identity() );

      task.unassign();

      task.sendTo( toOwner );
   }

   public void delegate( EntityReferenceDTO entity )
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Delegatee to = uowf.currentUnitOfWork().get( Delegatee.class, entity.entity().get().identity() );

      Owner owner = task.owner().get();

      Actor actor = getActor();

      if (task.isAssigned())
         task.unassign();

      task.delegateTo( to, actor, owner );
   }

   public void drop()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      Actor actor = getActor();

      if (!task.isAssigned())
      {
         task.assignTo( actor );
      }

      task.drop();
   }

   public void reactivate()
   {
      Status task = uowf.currentUnitOfWork().get( Status.class, getRequest().getAttributes().get( "task" ).toString() );
      task.reactivate();
   }

   public void redo()
   {
      Status task = uowf.currentUnitOfWork().get( Status.class, getRequest().getAttributes().get( "task" ).toString() );
      task.redo();
   }

   public void reject()
   {
      TaskEntity task = uowf.currentUnitOfWork().get( TaskEntity.class, getRequest().getAttributes().get( "task" ).toString() );

      task.unassign();
      task.rejectDelegation();
   }

   public void unassign()
   {
      Assignable task = uowf.currentUnitOfWork().get( Assignable.class, getRequest().getAttributes().get( "task" ).toString() );

      task.unassign();
   }

   public void deleteOperation()
   {
      Removable task = uowf.currentUnitOfWork().get( Removable.class, getRequest().getAttributes().get( "task" ).toString() );
      task.deleteEntity();
   }

   private Actor getActor()
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
            return uowf.currentUnitOfWork().get( Actor.class, userName );
         } else
            return null;
      }
   }
}
