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
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.organization.OwningOrganization;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/organizationalunits/{ou}/groups/{group}
 */
public class GroupServerResource
        extends CommandQueryServerResource
{
    public void changedescription(StringDTO stringValue) throws ResourceException
    {
        String groupId = (String) getRequest().getAttributes().get("group");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, groupId);

        String identity = getRequest().getAttributes().get("ou").toString();

        Groups.Data groups = uowf.currentUnitOfWork().get( Groups.Data.class, identity);
        checkPermission(groups);

        String newName = stringValue.string().get();

        describable.changeDescription(newName);
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String org = getRequest().getAttributes().get("ou").toString();

        Groups groups = uow.get(Groups.class, org);
        checkPermission(groups);
        String identity = getRequest().getAttributes().get("group").toString();
        GroupEntity group = uow.get(GroupEntity.class, identity);

        groups.removeGroup(group);
    }

    public ListValue findUsers(StringDTO query) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String orgId = getRequest().getAttributes().get("organization").toString();

        OwningOrganization organization  = uowf.currentUnitOfWork().get(OwningOrganization.class, orgId);
        checkPermission(organization);

        ListValue list = ((OrganizationQueries)organization.organization().get()).findUsers(query.string().get());

        String groupId = getRequest().getAttributes().get("group").toString();
        GroupEntity group = uow.get(GroupEntity.class,groupId);

        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        for(ListItemValue user : list.items().get())
        {
            if(!group.participants().contains(uow.get(Participant.class, user.entity().get().identity())))
            {
                listBuilder.addListItem(user.description().get(), user.entity().get());
            }
        }

        return listBuilder.newList();
    }

    public ListValue findGroups(StringDTO query) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String orgId = getRequest().getAttributes().get("organization").toString();

        OwningOrganization organization  = uowf.currentUnitOfWork().get(OwningOrganization.class, orgId);
        checkPermission(organization);

        ListValue list = ((OrganizationQueries)organization.organization().get()).findGroups(query.string().get());

        String groupId = getRequest().getAttributes().get("group").toString();
        GroupEntity group = uow.get(GroupEntity.class,groupId);

        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        for(ListItemValue grp : list.items().get())
        {
            if(!group.participants().contains(uow.get(Group.class, grp.entity().get().identity()))
               && !group.identity().get().equals(grp.entity().get().identity()))
            {
                listBuilder.addListItem(grp.description().get(), grp.entity().get());
            }
        }

        return listBuilder.newList();
    }

}
