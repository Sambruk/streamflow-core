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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.role.Role;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/policy/{administrator}
 */
public class AdministratorServerResource
        extends CommandQueryServerResource
{
    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String org = getRequest().getAttributes().get("organization").toString();

        OrganizationalUnitEntity ou = uow.get(OrganizationalUnitEntity.class, org);

        String identity = getRequest().getAttributes().get("administrator").toString();
        Participant participant = uow.get(Participant.class, identity);

        OrganizationEntity organization = (OrganizationEntity) ou.organization().get();
        Role adminRole = organization.roles().get(0);

        checkPermission(ou);

        ou.revokeRole(participant, adminRole);
    }
}