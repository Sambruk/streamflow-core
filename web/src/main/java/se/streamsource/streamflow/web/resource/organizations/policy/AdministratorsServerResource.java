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

package se.streamsource.streamflow.web.resource.organizations.policy;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.role.Role;
import se.streamsource.streamflow.web.domain.role.RolePolicy;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{organization}/administrators
 */
public class AdministratorsServerResource
        extends CommandQueryServerResource
{
    public ListValue administrators()
    {
        UnitOfWork unitOfWork = uowf.currentUnitOfWork();

        String identity = getRequest().getAttributes().get("organization").toString();
        OrganizationalUnit.OrganizationalUnitState ouState = unitOfWork.get(OrganizationalUnit.OrganizationalUnitState.class, identity);
        OrganizationEntity organization = (OrganizationEntity) ouState.organization().get();
        Role adminRole = organization.roles().get(0);

        RolePolicy.RolePolicyState rolePolicy = (RolePolicy.RolePolicyState) ouState;
        List<EntityReference> admins = rolePolicy.participantsWithRole(adminRole);
        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (EntityReference admin : admins)
        {
            Participant participant = unitOfWork.get(Participant.class, admin.identity());
            builder.addListItem(participant.getDescription(), admin);
        }
        return builder.newList();
    }

    public void addAdministrator(StringDTO participantId) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String identity = getRequest().getAttributes().get("organization").toString();

        OrganizationalUnitEntity ou = uow.get(OrganizationalUnitEntity.class, identity);
        Participant participant = uow.get(Participant.class, participantId.string().get());

        OrganizationEntity organization = (OrganizationEntity) ou.organization().get();
        Role adminRole = organization.roles().get(0);

        checkPermission(ou);
        ou.grantRole(participant, adminRole);
    }

}