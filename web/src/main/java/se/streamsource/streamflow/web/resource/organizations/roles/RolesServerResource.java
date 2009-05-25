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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.web.domain.project.Role;
import se.streamsource.streamflow.web.domain.project.RoleEntity;
import se.streamsource.streamflow.web.domain.project.Roles;
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
        for (Role role : roles.roles())
        {
            builder.addListItem(role.getDescription(), EntityReference.getEntityReference(role));
        }
        return builder.newList();
    }

    public void newRole(DescriptionValue value) throws DuplicateDescriptionException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<RoleEntity> builder = uow.newEntityBuilder(RoleEntity.class);

        String identity = getRequest().getAttributes().get("organization").toString();

        Roles roles = uow.get(Roles.class, identity);

        RoleEntity roleEntity = builder.prototype();
        roleEntity.description().set(value.description().get());

        Role role = builder.newInstance();

        roles.addRole(role);
    }
}