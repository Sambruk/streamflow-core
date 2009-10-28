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

package se.streamsource.streamflow.client.resource.organizations.organizationalunits;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.organizations.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
import se.streamsource.streamflow.client.resource.organizations.policy.AdministratorsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource;
import se.streamsource.streamflow.client.resource.organizations.roles.RolesClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchClientResource;

/**
 * JAVADOC
 */
public class OrganizationalUnitClientResource
        extends CommandQueryClientResource
{
    public OrganizationalUnitClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ProjectsClientResource projects() throws ResourceException
    {
        return getSubResource("projects", ProjectsClientResource.class);
    }

    public GroupsClientResource groups() throws ResourceException
    {
        return getSubResource("groups", GroupsClientResource.class);
    }

    public RolesClientResource roles() throws ResourceException
    {
        return getSubResource("roles", RolesClientResource.class);
    }

    public FormDefinitionsClientResource forms() throws ResourceException
    {
        return getSubResource("forms", FormDefinitionsClientResource.class);
    }

    public AdministratorsClientResource administrators() throws ResourceException
    {
        return getSubResource("administrators", AdministratorsClientResource.class);
    }

    public OrganizationalUnitsClientResource organizationalUnits() throws ResourceException
    {
        return getSubResource("organizationalunits", OrganizationalUnitsClientResource.class);
    }

    public SearchClientResource search()
    {
        return getSubResource("search", SearchClientResource.class);
    }
}