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

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.Event;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.project.ProjectRole;
import se.streamsource.streamflow.web.domain.project.Project;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnit.OrganizationalUnitMixin.class)
public interface OrganizationalUnit
{
    Organization getOrganization();

    void moveOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to) throws MoveOrganizationalUnitException;

    void mergeOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to) throws MergeOrganizationalUnitException;

    interface OrganizationalUnitState
    {
        Association<Organization> organization();

        @Event
        void organizationalUnitMoved(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to);

        @Event
        void organizationalUnitMerged(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to);
    }

    abstract class OrganizationalUnitMixin
            implements OrganizationalUnit, OrganizationalUnitState
    {
        @This
        OrganizationalUnitState state;

        public Organization getOrganization()
        {
            return state.organization().get();
        }

        public void moveOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to) throws MoveOrganizationalUnitException
        {
            OrganizationalUnitEntity uoe = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity target = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity source = (OrganizationalUnitEntity) from;
            if (uoe.identity().get().equals(target.identity().get()))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (target.organizationalUnits().contains(uoe))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (!source.organizationalUnits().contains(uoe))
            {
                throw new MoveOrganizationalUnitException();
            }

            organizationalUnitMoved(DomainEvent.CREATE, from , to);
        }

        public void mergeOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to) throws MergeOrganizationalUnitException
        {
            OrganizationalUnitEntity uoe = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity target = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity source = (OrganizationalUnitEntity) from;
            if (uoe.identity().get().equals(target.identity().get()))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (target.organizationalUnits().contains(uoe))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (!source.organizationalUnits().contains(uoe))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (uoe.organizationalUnits().count() != 0)
            {
                throw new MergeOrganizationalUnitException();
            }

            organizationalUnitMerged(DomainEvent.CREATE, from, to);
        }


        public void organizationalUnitMoved(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to)
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity fromEntity = (OrganizationalUnitEntity) from;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;

            fromEntity.organizationalUnits().remove(oue);
            toEntity.organizationalUnits().add(oue);
        }

        public void organizationalUnitMerged(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to)
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity fromEntity = (OrganizationalUnitEntity) from;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;

            fromEntity.organizationalUnits().remove(oue);

            // TODO fix duplicated names
            // duplicate role names (case sensitive?)
            // move usage to existing role then delete role
            for (ProjectRole role : fromEntity.roles())
            {
                fromEntity.roles().remove(role);
                toEntity.roles().add(role);
            }

            // duplicate group name: Idea: change groupname to somehitng unique (prefix by old OU name)
            for (Group group: fromEntity.groups())
            {
                fromEntity.groups().remove(group);
                toEntity.groups().add(group);
            }

            // duplicate project name: same as for group
            for (Project project: fromEntity.projects())
            {
                fromEntity.projects().remove(project);
                toEntity.projects().add(project);
            }
        }
    }
}
