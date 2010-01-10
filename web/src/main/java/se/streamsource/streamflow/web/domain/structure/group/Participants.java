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

package se.streamsource.streamflow.web.domain.structure.group;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@SideEffects(Participants.RemovableSideEffect.class)
@Mixins(Participants.Mixin.class)
public interface Participants
{
   void addParticipant( Participant newParticipant );

   void removeParticipant( Participant participant );

   /**
    * Check if given participant is a participant of this collection.
    * The check is recursive, so if any participant is a collection,
    * check that collection as well.
    *
    * @param participant the participant to check
    * @return true if participant is a member of this collection or any collection herein
    */
   boolean isParticipant( Participant participant );

   interface Data
   {
      ManyAssociation<Participant> participants();

      void addedParticipant( DomainEvent event, Participant participant );

      void removedParticipant( DomainEvent event, Participant participant );
   }

   abstract class Mixin
         implements Participants, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      Group group;

      public void addParticipant( Participant participant )
      {
         if (!participants().contains( participant ))
         {
            addedParticipant( DomainEvent.CREATE, participant );
            participant.joinGroup( group );
         }
      }

      public void removeParticipant( Participant participant )
      {
         if (participants().contains( participant ))
         {
            removedParticipant( DomainEvent.CREATE, participant );
            participant.leaveGroup( group );
         }
      }

      public boolean isParticipant( Participant participant )
      {
         for (Participant participant1 : participants())
         {
            if (participant.equals(participant1))
               return true;

            if (participant1 instanceof Participants)
            {
               Participants participants = (Participants) participant1;
               if (participants.isParticipant( participant ))
                  return true;
            }
         }

         return false;
      }

      // Events
      public void addedParticipant( DomainEvent event, Participant participant )
      {
         participants().add( participant );
      }

      public void removedParticipant( DomainEvent event, Participant participant )
      {
         participants().remove( participant );
      }

   }

   class RemovableSideEffect
         extends SideEffectOf<Removable>
         implements Removable
   {
      @This
      Participants.Data state;

      @This
      Participants participants;

      public boolean removeEntity()
      {
         if (result.removeEntity())
         {
            // Make participants leave
            for (Participant participant : state.participants().toList())
            {
               participants.removeParticipant( participant );
            }
         }

         return true;
      }

      public boolean reinstate()
      {
         return result.reinstate();
      }

      public void deleteEntity()
      {
         result.deleteEntity();
      }
   }

}
