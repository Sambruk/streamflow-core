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

package se.streamsource.streamflow.web.domain.group;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@SideEffects(Participants.RemovableSideEffect.class)
@Mixins(Participants.ParticipantsMixin.class)
public interface Participants
{
    void addParticipant(Participant newParticipant);

    void removeParticipant(Participant participant);

    interface ParticipantsState
    {
        ManyAssociation<Participant> participants();


        void participantJoined(DomainEvent event, Participant participant);


        void participantRemoved(DomainEvent event, Participant participant);
    }

    abstract class ParticipantsMixin
            implements Participants, ParticipantsState
    {
        @Structure
        ValueBuilderFactory vbf;

        @This
        Group group;

        public void addParticipant(Participant participant)
        {
            if (!participants().contains(participant))
            {
                participantJoined(DomainEvent.CREATE, participant);
                participant.joinGroup(group);
            }
        }

        public void removeParticipant(Participant participant)
        {
            if (participants().contains(participant))
            {
                participantRemoved(DomainEvent.CREATE, participant);
                participant.leaveGroup(group);
            }
        }

        // Events
        public void participantJoined(DomainEvent event, Participant participant)
        {
            participants().add(participant);
        }

        public void participantRemoved(DomainEvent event, Participant participant)
        {
            participants().remove(participant);
        }

    }

    class RemovableSideEffect
            extends SideEffectOf<Removable>
            implements Removable
    {
        @This ParticipantsState state;
        @This Participants participants;

        public boolean removeEntity()
        {
            if (result.removeEntity())
            {
                // Make participants leave
                for (Participant participant : state.participants().toList())
                {
                    participants.removeParticipant(participant);
                }
            }

            return true;
        }

        public boolean reinstate()
        {
            return result.reinstate();
        }
    }

}
