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
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectRole;
import se.streamsource.streamflow.web.domain.project.ProjectRoleEntity;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnit.OrganizationalUnitMixin.class)
public interface OrganizationalUnit
{
    void moveOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MoveOrganizationalUnitException;

    void mergeOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MergeOrganizationalUnitException;

    interface OrganizationalUnitState
    {
        Association<Organization> organization();
    }

    abstract class OrganizationalUnitMixin
            implements OrganizationalUnit, OrganizationalUnitState
    {
        @This
        OrganizationalUnitState state;

        public void moveOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MoveOrganizationalUnitException
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            if (oue.identity().get().equals(toEntity.identity().get()))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (toEntity.organizationalUnits().contains(oue))
            {
                throw new MoveOrganizationalUnitException();
            }

            if (!parentEntity.organizationalUnits().contains(oue))
            {
                throw new MoveOrganizationalUnitException();
            }

            parentEntity.organizationalUnitRemoved(DomainEvent.CREATE, oue);
            toEntity.organizationalUnitAdded(DomainEvent.CREATE, oue);
        }

        public void mergeOrganizationalUnit(OrganizationalUnit parent, OrganizationalUnit to) throws MergeOrganizationalUnitException
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity parentEntity = (OrganizationalUnitEntity) parent;
            if (oue.identity().get().equals(toEntity.identity().get()))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (!parentEntity.organizationalUnits().contains(oue))
            {
                throw new MergeOrganizationalUnitException();
            }

            if (oue.organizationalUnits().count() != 0)
            {
                throw new MergeOrganizationalUnitException();
            }

            while (oue.projectRoles().count() > 0)
            {
                ProjectRole role = oue.projectRoles().get(0);
                oue.projectRoleRemoved(DomainEvent.CREATE, role);
                toEntity.projectRoleAdded(DomainEvent.CREATE, (ProjectRoleEntity) role);
            }
            while (oue.groups().count() >0)
            {
                Group group = oue.groups().get(0);
                oue.groupRemoved(DomainEvent.CREATE, group);
                toEntity.groupAdded(DomainEvent.CREATE, (GroupEntity) group);
            }
            while (oue.projects().count() >0)
            {
                Project project = oue.projects().get(0);
                oue.projectRemoved(DomainEvent.CREATE, project);
                toEntity.projectAdded(DomainEvent.CREATE, project);
            }

            parentEntity.organizationalUnitRemoved(DomainEvent.CREATE, oue);
            oue.removeEntity();
        }
    }
}
