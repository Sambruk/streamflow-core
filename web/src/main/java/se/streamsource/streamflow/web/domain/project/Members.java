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
import se.streamsource.streamflow.web.domain.group.Participant;

/**
 * JAVADOC
 */
@SideEffects(Members.RemovableSideEffect.class)
@Mixins(Members.Mixin.class)
public interface Members
{
    void addMember(Participant participant);

    void removeMember(Participant participant);

    void removeAllMembers();

    interface Data
    {
        ManyAssociation<Participant> members();

        void addedMember(DomainEvent event, Participant participant);

        void removedMember(DomainEvent event, Participant participant);
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

        public void addMember(Participant participant)
        {
            if (members().contains(participant))
                return;

            addedMember(DomainEvent.CREATE, participant);

            participant.joinProject(project);
        }

        public void removeMember(Participant participant)
        {
            if (!members().contains(participant))
            {
                return;
            }
            removedMember(DomainEvent.CREATE, participant);
            participant.leaveProject(project);
        }

        public void removeAllMembers()
        {
            while (members().count() != 0)
            {
                removeMember(members().get(0));
            }
        }
    }

    abstract class RemovableSideEffect
        extends SideEffectOf<Removable>
        implements Removable
    {
        @This Members members;

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
