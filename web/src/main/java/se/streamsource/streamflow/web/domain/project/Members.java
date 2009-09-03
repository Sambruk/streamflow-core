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

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.group.Participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JAVADOC
 */
@Concerns(Members.LifecycleConcern.class)
@Mixins(Members.MembersMixin.class)
public interface Members
{
    void createMember(Participant participant);

    void addRole(Participant participant, Role role);

    void removeMember(Participant participant);

    void removeRole(Participant participant, Role role);

    interface MembersState
    {
        Property<MembersValue> members();

        boolean isMember(Participant participant);

        Iterable<Role> getRoles(Participant participant);
    }

    abstract class MembersMixin
            implements Members, MembersState
    {
        @This
        MembersState state;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Project project;

        public boolean isMember(Participant participant)
        {
            EntityReference participantRef = EntityReference.getEntityReference(participant);
            MembersValue membersValue = state.members().get();
            MemberValue memberValue = membersValue.getMemberValue(participantRef);
            return memberValue != null;
        }

        public void createMember(Participant participant)
        {
            if (isMember(participant))
                return;
            ValueBuilder<MemberValue> builder = vbf.newValueBuilder(MemberValue.class);
            builder.prototype().participant().set(EntityReference.getEntityReference(participant));
            ValueBuilder<MembersValue> membersBuilder = state.members().get().buildWith();
            List<MemberValue> members = membersBuilder.prototype().members().get();
            members.add(builder.newInstance());
            state.members().set(membersBuilder.newInstance());
            participant.addProject(project);
        }

        public void addRole(Participant participant, Role role)
        {
            EntityReference participantRef = EntityReference.getEntityReference(participant);
            ValueBuilder<MembersValue> membersBuilder = state.members().get().buildWith();
            MemberValue memberValue = membersBuilder.prototype().getMemberValue(participantRef);
            if (memberValue != null)
            {
                List<EntityReference> roles = memberValue.roles().get();
                EntityReference roleRef = EntityReference.getEntityReference(role);
                for (EntityReference entityReference : roles)
                {
                    if (entityReference.equals(roleRef))
                        return;
                }

                roles.add(roleRef);

                state.members().set(membersBuilder.newInstance());
            }
        }

        public void removeMember(Participant participant)
        {
            EntityReference participantRef = EntityReference.getEntityReference(participant);
            ValueBuilder<MembersValue> membersBuilder = state.members().get().buildWith();
            MemberValue memberValue = membersBuilder.prototype().getMemberValue(participantRef);
            if (memberValue != null)
            {
                membersBuilder.prototype().members().get().remove(memberValue);
                state.members().set(membersBuilder.newInstance());
                participant.removeProject(project);
            }
        }

        public void removeRole(Participant participant, Role role)
        {
            EntityReference participantRef = EntityReference.getEntityReference(participant);
            EntityReference roleRef = EntityReference.getEntityReference(role);
            ValueBuilder<MembersValue> membersBuilder = state.members().get().buildWith();
            MemberValue memberValue = membersBuilder.prototype().getMemberValue(participantRef);
            if (memberValue != null)
            {
                if (memberValue.roles().get().remove(roleRef))
                    state.members().set(membersBuilder.newInstance());
            }
        }

        public Iterable<Role> getRoles(Participant participant)
        {
            EntityReference participantRef = EntityReference.getEntityReference(participant);
            MemberValue memberValue = state.members().get().getMemberValue(participantRef);
            if (memberValue != null)
            {
                List<Role> roles = new ArrayList<Role>();
                for (EntityReference entityReference : memberValue.roles().get())
                {
                    roles.add(uowf.currentUnitOfWork().get(Role.class, entityReference.identity()));
                }
                return roles;
            } else
                return Collections.emptyList();
        }
    }

    class LifecycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        @This
        MembersState state;
        @Structure
        ValueBuilderFactory vbf;

        public void create() throws LifecycleException
        {
            state.members().set(vbf.newValue(MembersValue.class));
        }

        public void remove() throws LifecycleException
        {
        }
    }

}
