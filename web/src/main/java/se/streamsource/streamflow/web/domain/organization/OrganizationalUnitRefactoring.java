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
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.domain.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.project.Projects;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnitRefactoring.Mixin.class)
public interface OrganizationalUnitRefactoring
{
    void moveOrganizationalUnit(OrganizationalUnits to) throws MoveOrganizationalUnitException;

    void mergeOrganizationalUnit( OrganizationalUnitRefactoring to) throws MergeOrganizationalUnitException;

    void deleteOrganizationalUnit() throws OpenProjectExistsException;

    interface Data
    {
        Association<Organization> organization();

        OrganizationalUnits getParent();
    }

    abstract class Mixin
            implements OrganizationalUnitRefactoring, Data
    {
        @This
        Projects.Data projects;

        @This
        ProjectRoles projectRoles;

        @This
        Groups groups;

        @This
        OrganizationalUnits.Data organizationalUnits;

        @This
        Removable removable;

        @This
        OrganizationalUnit organizationalUnit;


        public void deleteOrganizationalUnit() throws OpenProjectExistsException
        {
            if(projects.projects().count() > 0)
            {
                throw new OpenProjectExistsException("There are open projects");
            }
            else
            {
                for(OrganizationalUnitRefactoring oue : organizationalUnits.organizationalUnits())
                {
                    OrganizationalUnitEntity e = (OrganizationalUnitEntity)oue;

                    if(e.projects().count() > 0)
                     {
                         throw new OpenProjectExistsException("There are open projects");
                     }
                }
            }
            OrganizationalUnits parent = getParent();
            parent.removeOrganizationalUnit(organizationalUnit);
            removable.removeEntity();
        }

        public void moveOrganizationalUnit(OrganizationalUnits to) throws MoveOrganizationalUnitException
        {
            OrganizationalUnits parent = getParent();
            if (organizationalUnit.equals(to))
            {
                throw new MoveOrganizationalUnitException();
            }
            if (to.equals(parent))
            {
                return;
            }

            parent.removeOrganizationalUnit(organizationalUnit);
            to.addOrganizationalUnit(organizationalUnit);
        }

        public void mergeOrganizationalUnit( OrganizationalUnitRefactoring to) throws MergeOrganizationalUnitException
        {
            OrganizationalUnits parent = getParent();
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;
            if (organizationalUnit.equals(toEntity))
            {
                throw new MergeOrganizationalUnitException();
            }

            projectRoles.mergeProjectRoles((ProjectRoles)to);
            groups.mergeGroups((Groups)to);
            projects.mergeProjects((Projects)to);

            parent.removeOrganizationalUnit(organizationalUnit);
            removable.removeEntity();
        }

        public OrganizationalUnits getParent()
        {
            OrganizationalUnits.Data ous = (OrganizationalUnits.Data) organization().get();
            return ous.getParent(organizationalUnit);
        }
    }
}
