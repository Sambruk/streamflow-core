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

import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.injection.scope.This;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.project.Project;

/**
 * JAVADOC
 */
@SideEffects(GroupEntity.RemovableLifeycleSideEffect.class)
public interface GroupEntity
        extends Group,
        Describable.DescribableState,
        Participant.ParticipantState,
        Participants.ParticipantsState,
        Removable.RemovableState,
        DomainEntity
{
    class RemovableLifeycleSideEffect
            extends SideEffectOf<Removable>
            implements Removable
    {
        @This GroupEntity thisGroup;

        public boolean removeEntity()
        {
            if (result.removeEntity())
            {
                // Make participants leave
                for (Participant participant : thisGroup.participants().toList())
                {
                    thisGroup.removeParticipant(participant);
                }

                // Leave other groups and projects
                for (Group group : thisGroup.groups().toList())
                {
                    group.removeParticipant(thisGroup);
                }

                for (Project project : thisGroup.projects().toList())
                {
                    project.removeMember(thisGroup);
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
