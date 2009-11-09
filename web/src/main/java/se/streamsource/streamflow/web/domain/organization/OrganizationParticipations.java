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

package se.streamsource.streamflow.web.domain.organization;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.role.ParticipantRolesValue;
import se.streamsource.streamflow.web.domain.role.Role;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.List;

/**
 * List of organizations a participant is a member of.
 */
 @Concerns(OrganizationParticipations.LeaveConcern.class)
@Mixins(OrganizationParticipations.Mixin.class)
public interface OrganizationParticipations
{
    void join(Organization org);

    void leave(Organization ou);

    interface Data
    {
        ManyAssociation<Organization> organizations();

        void joinedOrganization(DomainEvent event, Organization org);
        void leftOrganization(DomainEvent event, Organization org);
    }

    abstract class Mixin
            implements OrganizationParticipations, Data
    {
        @This
        Data state;

        public void join(Organization ou)
        {
            if (!state.organizations().contains(ou))
            {
                joinedOrganization(DomainEvent.CREATE, ou);
            }
        }

        public void leave(Organization ou)
        {
            if (state.organizations().contains(ou))
            {
                leftOrganization(DomainEvent.CREATE, ou);
            }
        }

        public void joinedOrganization(DomainEvent event, Organization org)
        {
            state.organizations().add(org);
        }

        public void leftOrganization(DomainEvent event, Organization org)
        {
            state.organizations().remove(org);
        }
    }

    abstract class LeaveConcern
        extends ConcernOf<OrganizationParticipations>
        implements OrganizationParticipations
    {
        @This
        UserEntity user;
        
        @Structure
        UnitOfWorkFactory uowf;

        public void leave(Organization ou)
        {
            userLeaves((OrganizationalUnitEntity) ou);
            next.leave(ou);
        }

        private boolean userLeaves(OrganizationalUnitEntity org)
        {
            for(ParticipantRolesValue participantRoleValue : org.policy().get())
            {
                if(participantRoleValue.participant().get().equals(EntityReference.getEntityReference(user)))
                {
                    for (EntityReference reference : participantRoleValue.roles().get())
                    {
                        org.revokeRole(user,uowf.currentUnitOfWork().get(Role.class, reference.identity()));
                    }

                }
            }

            // leave project
            for(Project project : org.projects())
            {
                user.leaveProject(project);
            }

            List<Group> groupList = user.groups().toList();
            for(Group group : org.groups())
            {
                if(groupList.contains(group))
                {
                    user.leaveGroup(group);
                }
            }

            for(OrganizationalUnit orgUnit : org.organizationalUnits())
            {
                userLeaves((OrganizationalUnitEntity)orgUnit);
            }

            return true;
        }
    }
}
