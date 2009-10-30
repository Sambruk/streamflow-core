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

package se.streamsource.streamflow.web.domain.role;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import static org.qi4j.api.entity.EntityReference.*;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.user.UserAuthentication;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Policy for managging Roles assigned to Participants. Participants
 * can have a list of Roles assigned to them, which can be granted and revoked.
 */
@Mixins(RolePolicy.Mixin.class)
public interface RolePolicy
{
    void grantRole(Participant participant, Role role);

    void revokeRole(Participant participant, Role role);

    void grantAdministratorToCurrentUser();

    interface Data
    {
        @UseDefaults
        Property<List<ParticipantRolesValue>> policy();

        void grantedRole(DomainEvent event, Participant participant, Role role);

        void revokedRole(DomainEvent event, Participant participant, Role role);

        boolean participantHasRole(Participant participant, Role role);

        List<EntityReference> participantsWithRole(Role role);

        boolean hasRoles(Participant participant);
    }

    abstract class Mixin
            implements RolePolicy, Data, UserPermissions
    {
        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        OrganizationalUnitRefactoring.Data ouState;

        public void grantRole(Participant participant, Role role)
        {
            if (participantHasRole(participant, role))
                return;

            grantedRole(DomainEvent.CREATE, participant,  role);
        }

        public void revokeRole(Participant participant, Role role)
        {
            if (!participantHasRole(participant, role))
                return;

            revokedRole(DomainEvent.CREATE, participant,  role);
        }

        public void grantAdministratorToCurrentUser()
        {
            Subject subject = Subject.getSubject(AccessController.getContext());
            if (subject != null)
            {
                Principal principal = subject.getPrincipals().iterator().next();
                Participant user = uowf.currentUnitOfWork().get(Participant.class, principal.getName());
                OrganizationEntity org = (OrganizationEntity) ouState.organization().get();
                Role administrator = org.getAdministratorRole();
                grantRole(user, administrator);
            }
        }

        public void grantedRole(DomainEvent event, Participant participant, Role role)
        {
            EntityReference participantRef = getEntityReference(participant);
            List<ParticipantRolesValue> participantRoles = policy().get();
            int idx = 0;
            for (ParticipantRolesValue participantRole : participantRoles)
            {
                if (participantRole.participant().get().equals(participantRef))
                {
                    // Add role to list
                    EntityReference roleRef = getEntityReference(role);
                    ValueBuilder<ParticipantRolesValue> builder = participantRole.buildWith();
                    builder.prototype().roles().get().add(roleRef);
                    participantRoles.set(idx, builder.newInstance());
                    policy().set(participantRoles);
                    return;
                }
                idx++;
            }

            // Participant is not in list - add it
            EntityReference roleRef = getEntityReference(role);
            ValueBuilder<ParticipantRolesValue> builder = vbf.newValueBuilder(ParticipantRolesValue.class);
            builder.prototype().participant().set(participantRef);
            builder.prototype().roles().get().add(roleRef);
            List<ParticipantRolesValue> policy = policy().get();
            policy.add(builder.newInstance());
            policy().set(policy);
        }

        public void revokedRole(DomainEvent event, Participant participant, Role role)
        {
            EntityReference participantRef = getEntityReference(participant);
            List<ParticipantRolesValue> participantRoles = policy().get();
            int idx = 0;
            for (ParticipantRolesValue participantRole : participantRoles)
            {
                if (participantRole.participant().get().equals(participantRef))
                {
                    // Remove role from list
                    EntityReference roleRef = getEntityReference(role);
                    ValueBuilder<ParticipantRolesValue> builder = participantRole.buildWith();
                    builder.prototype().roles().get().remove(roleRef);
                    participantRoles.set(idx, builder.newInstance());
                    policy().set(participantRoles);
                    return;
                }
                idx++;
            }
        }

        public boolean participantHasRole(Participant participant, Role role)
        {
            // Check if user already has role
            ParticipantRolesValue participantRolesValue = getRoles(participant);
            if (participantRolesValue != null)
            {
                EntityReference roleRef = getEntityReference(role);
                for (EntityReference participantRole : participantRolesValue.roles().get())
                {
                    if (participantRole.equals(roleRef))
                        return true;
                }
            }
            return false;
        }

        public ParticipantRolesValue getRoles(Participant participant)
        {
            EntityReference participantRef = getEntityReference(participant);
            for (ParticipantRolesValue participantRolesValue : policy().get())
            {
                if (participantRolesValue.participant().get().equals(participantRef))
                {
                    return participantRolesValue;
                }
            }
            return null;
        }

        public List<EntityReference> participantsWithRole(Role role)
        {
            List<EntityReference> participants = new ArrayList<EntityReference>();
            EntityReference roleRef = getEntityReference(role);
            for (ParticipantRolesValue participantRolesValue : policy().get())
            {
                for (EntityReference participantRole : participantRolesValue.roles().get())
                {
                    if (participantRole.equals(roleRef))
                    {
                        participants.add(participantRolesValue.participant().get());
                        break;
                    }
                }

            }
            return participants;
        }

        public boolean hasRoles(Participant participant)
        {
            ParticipantRolesValue value = getRoles(participant);
            return value != null && !value.roles().get().isEmpty();
        }

        public PermissionCollection getPermissions( UserAuthentication user)
        {
            PermissionCollection permissions = null;

            // If participant has any role, it's the Admin role -> AllPermissions
            if (hasRoles((Participant) user))
            {
                permissions = new AllPermission().newPermissionCollection();
                permissions.add(new AllPermission());
            }
            
            return permissions;
        }
    }
}
