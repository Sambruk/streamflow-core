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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.group.Participants;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/groups/{group}
 */
public class GroupServerResource
        extends CommandQueryServerResource
{
    /*public ListValue participants()
    {
        ListValueBuilder builder = new ListValueBuilder(vbf);
        UnitOfWork uow = uowf.currentUnitOfWork();
        String identity = getRequest().getAttributes().get("group").toString();
        Participants.ParticipantsState participants = uow.get(Participants.ParticipantsState.class, identity);
        for (Participant participant : participants.participants())
        {
            builder.addListItem(participant.participantDescription(), EntityReference.getEntityReference(participant));
        }
        return builder.newList();
    }*/

    @Override
    protected Representation delete() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete group"));

        String org = getRequest().getAttributes().get("organization").toString();

        Groups groups = uow.get(Groups.class, org);

        String identity = getRequest().getAttributes().get("group").toString();
        GroupEntity group = uow.get(GroupEntity.class, identity);

        groups.removeGroup(group);

        uow.remove(group);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }

        return null;
    }

    /*public void addParticipant(EntityReferenceValue participantId)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Participant participant = uow.get(Participant.class, participantId.entity().get().identity());

        String identity = getRequest().getAttributes().get("group").toString();
        Participants participants = uow.get(GroupEntity.class, identity);

        participants.addParticipant(participant);
    }*/
}
