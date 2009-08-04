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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.OrganizationClientResource;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;

/**
 * JAVADOC
 */
public class OrganizationalUnitAdministrationModel
    implements Refreshable
{
    private GroupsModel groupsModel;
    private ProjectsModel projectsModel;
    private RolesModel rolesModel;
    private OrganizationClientResource organization;

    public OrganizationalUnitAdministrationModel(@Structure ObjectBuilderFactory obf, @Uses OrganizationClientResource organization) throws ResourceException
    {
        this.organization = organization;
        groupsModel = obf.newObjectBuilder(GroupsModel.class).use(organization.groups()).newInstance();
        projectsModel = obf.newObjectBuilder(ProjectsModel.class).use(organization.projects(), this).newInstance();
        rolesModel = obf.newObjectBuilder(RolesModel.class).use(organization.roles()).newInstance();
    }

    public OrganizationClientResource getOrganization()
    {
        return organization;
    }

    public GroupsModel groupsModel()
    {
        return groupsModel;
    }

    public ProjectsModel projectsModel()
    {
        return projectsModel;
    }

    public RolesModel rolesModel()
    {
        return rolesModel;
    }

    public void refresh() throws ResourceException
    {
        groupsModel.refresh();
        projectsModel.refresh();
        rolesModel.refresh();
    }
}
