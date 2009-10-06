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

package se.streamsource.streamflow.web.resource.organizations.roles;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.role.Role;
import se.streamsource.streamflow.web.domain.role.Roles;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/roles
 */
public class RolesServerResource
        extends CommandQueryServerResource
{
    public ListValue roles()
    {
        String identity = getRequest().getAttributes().get("organization").toString();
        Roles.RolesState roles = uowf.currentUnitOfWork().get(Roles.RolesState.class, identity);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (Role projectRole : roles.roles())
        {
            builder.addListItem(projectRole.getDescription(), EntityReference.getEntityReference(projectRole));
        }
        return builder.newList();
    }

    public void createRole(StringDTO name) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String identity = getRequest().getAttributes().get("organization").toString();

        ProjectRoles projectRoles = uow.get(ProjectRoles.class, identity);

        checkPermission(projectRoles);
        projectRoles.createProjectRole(name.string().get());
    }

}