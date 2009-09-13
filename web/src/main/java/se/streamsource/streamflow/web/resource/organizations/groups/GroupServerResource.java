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

package se.streamsource.streamflow.web.resource.organizations.groups;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/groups/{group}
 */
public class GroupServerResource
        extends CommandQueryServerResource
{
    public void describe(StringDTO stringValue) throws ResourceException
    {
        String groupId = (String) getRequest().getAttributes().get("group");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, groupId);

        String identity = getRequest().getAttributes().get("organization").toString();

        Groups.GroupsState groups = uowf.currentUnitOfWork().get(Groups.GroupsState.class, identity);
        checkPermission(groups);

        String newName = stringValue.string().get();
        for (Group group : groups.groups())
        {
            if (group.hasDescription(newName))
            {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
            }
        }

        describable.describe(newName);
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete group"));

        String org = getRequest().getAttributes().get("organization").toString();

        Groups groups = uow.get(Groups.class, org);
        try
        {
            checkPermission(groups);
        } catch(AccessControlException ae)
        {
            uow.discard();
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }

        String identity = getRequest().getAttributes().get("group").toString();
        GroupEntity group = uow.get(GroupEntity.class, identity);

        groups.removeGroup(group);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }
    }

}
