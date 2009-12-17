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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.group.Participants;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.user.User;

/**
 * JAVADOC
 */
@SideEffects(Members.RemovableSideEffect.class)
@Mixins(Members.Mixin.class)
public interface Members
{
   void addMember( Participant participant );

   void removeMember( Participant participant );

   void removeAllMembers();

   boolean isMember(Participant participant);

   interface Data
   {
      ManyAssociation<Participant> members();

      void addedMember( DomainEvent event, Participant participant );

      void removedMember( DomainEvent event, Participant participant );
   }

   abstract class Mixin
         implements Members, Data
   {
      @This
      Data state;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Project project;

      public void addMember( Participant participant )
      {
         if (members().contains( participant ))
            return;

         addedMember( DomainEvent.CREATE, participant );

         participant.joinProject( project );
      }

      public void removeMember( Participant participant )
      {
         if (!members().contains( participant ))
         {
            return;
         }
         // Get all active tasks in a project for a particular user and unassign.
         for (TaskDTO taskDTO : ((ProjectEntity) project).assignmentsTasks( (User) participant ).tasks().get())
         {
            Task task = uowf.currentUnitOfWork().get( Task.class, taskDTO.task().get().identity() );
            task.unassign();
         }
         removedMember( DomainEvent.CREATE, participant );
         participant.leaveProject( project );
      }

      public void removeAllMembers()
      {
         while (members().count() != 0)
         {
            removeMember( members().get( 0 ) );
         }
      }

      public boolean isMember( Participant participant )
      {
         for (Participant participant1 : members())
         {
            if (participant1.equals(participant))
               return true;

            if (participant1 instanceof Participants)
            {
               Participants participants = (Participants) participant1;
               if (participants.isParticipant(participant))
                  return true;
            }
         }

         return false;
      }
   }

   abstract class RemovableSideEffect
         extends SideEffectOf<Removable>
         implements Removable
   {
      @This
      Members members;

      public boolean removeEntity()
      {
         if (result.removeEntity())
         {
            // Remove all members from the project
            members.removeAllMembers();
         }

         return true;
      }
   }
}
