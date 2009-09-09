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

package se.streamsource.streamflow.web.resource.organizations.groups.participants;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.group.Participants;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/groups/{group}/participants
 */
public class ParticipantsServerResource
        extends CommandQueryServerResource
{
    public ListValue participants()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String identity = getRequest().getAttributes().get("organization").toString();
        Groups.GroupsState groups = uow.get(Groups.GroupsState.class, identity);
        checkPermission(groups);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        String groupId = getRequest().getAttributes().get("group").toString();
        for (Group group : groups.groups())
        {
            if (group.identity().get().equals(groupId))
            {
                Participants.ParticipantsState participants = uow.get(Participants.ParticipantsState.class, groupId);
                for (Participant participant : participants.participants())
                {
                    builder.addListItem(participant.getDescription(), EntityReference.getEntityReference(participant));
                }
                return builder.newList();
            }
        }

        return builder.newList();
    }
}