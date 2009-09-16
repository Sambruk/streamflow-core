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

    void moveOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MoveOrganizationalUnitException;

    void mergeOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MergeOrganizationalUnitException;

    interface OrganizationalUnitState
    {
        Association<Organization> organization();

        @Event
        void organizationalUnitMoved(DomainEvent event, OrganizationalUnit parent, OrganizationalUnit to);

        @Event
        void organizationalUnitMerged(DomainEvent event, OrganizationalUnit parent, OrganizationalUnit to);
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

        public void moveOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MoveOrganizationalUnitException
        {
            OrganizationalUnitEntity uoe = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity target = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            if (uoe.identity().get().equals(target.identity().get()))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (target.organizationalUnits().contains(uoe))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (!parentEntity.organizationalUnits().contains(uoe))
            {
                throw new MoveOrganizationalUnitException();
            }

            organizationalUnitMoved(DomainEvent.CREATE, parent , to);
        }

        public void mergeOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MergeOrganizationalUnitException
        {
            OrganizationalUnitEntity uoe = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity target = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            if (uoe.identity().get().equals(target.identity().get()))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (target.organizationalUnits().contains(uoe))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (!parentEntity.organizationalUnits().contains(uoe))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (uoe.organizationalUnits().count() != 0)
            {
                throw new MergeOrganizationalUnitException();
            }

            organizationalUnitMerged(DomainEvent.CREATE, parent, to);
        }


        public void organizationalUnitMoved(DomainEvent event, OrganizationalUnit parent, OrganizationalUnit to)
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;

            parentEntity.organizationalUnits().remove(oue);
            toEntity.organizationalUnits().add(oue);
        }

        public void organizationalUnitMerged(DomainEvent event, OrganizationalUnit parent, OrganizationalUnit to)
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;

            parentEntity.organizationalUnits().remove(oue);

            while (oue.roles().count() > 0)
            {
                ProjectRole role = oue.roles().get(0);
                toEntity.roles().add(role);
                oue.roles().remove(role);
            }
            while (oue.groups().count() >0)
            {
                Group group = oue.groups().get(0);
                toEntity.groups().add(group);
                oue.groups().remove(group);
            }
            while (oue.projects().count() >0)
            {
                Project project = oue.projects().get(0);
                toEntity.projects().add(project);
                oue.projects().remove(project);
            }
            oue.remove();
        }
    }
}
