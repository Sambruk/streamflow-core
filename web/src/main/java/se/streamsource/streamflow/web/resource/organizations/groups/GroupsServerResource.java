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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.io.IOException;

/**
 * Mapped to:
 * /organizations/{organization}/groups
 */
public class GroupsServerResource
        extends CommandQueryServerResource
{

    // get???
    public ListValue groups()
    {
        String identity = getRequest().getAttributes().get("organization").toString();
        Groups.GroupsState groups = uowf.currentUnitOfWork().get(Groups.GroupsState.class, identity);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (Group group : groups.groups())
        {
            builder.addListItem(group.getDescription(), EntityReference.getEntityReference(group));
        }
        
        return builder.newList();
    }

    @Override
    protected Representation post(Representation representation, Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Create Group"));
        EntityBuilder<GroupEntity> builder = uow.newEntityBuilder(GroupEntity.class);

        String identity = getRequest().getAttributes().get("organization").toString();

        Groups groups = uow.get(Groups.class, identity);

        GroupEntity groupState = builder.prototype();
        try
        {
            groupState.description().set(representation.getText());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Group group = builder.newInstance();

        try
        {
            groups.addGroup(group);
        } catch (DuplicateDescriptionException e)
        {
            //throw new ResourceException
            e.printStackTrace();
        }

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
        }

        return null;
    }
}
