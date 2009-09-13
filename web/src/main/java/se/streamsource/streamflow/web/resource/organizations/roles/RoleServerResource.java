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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.project.RoleEntity;
import se.streamsource.streamflow.web.domain.project.Roles;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/roles/{role}
 */
public class RoleServerResource
        extends CommandQueryServerResource
{
    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete role"));

        String org = getRequest().getAttributes().get("organization").toString();

        Roles roles = uow.get(Roles.class, org);

        String identity = getRequest().getAttributes().get("role").toString();
        RoleEntity role = uow.get(RoleEntity.class, identity);

        try
        {
            checkPermission(roles);
        } catch(AccessControlException e)
        {
            uow.discard();
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }

        roles.removeRole(role);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }
    }
}