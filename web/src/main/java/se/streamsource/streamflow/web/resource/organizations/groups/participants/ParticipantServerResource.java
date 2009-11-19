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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/groups/{group}/participants/{participant}
 */
public class ParticipantServerResource
        extends CommandQueryServerResource
{
    //TODO do a check if the orgunit actually contains the project
    public void putOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String participantId = getRequest().getAttributes().get("participant").toString();
        Participant participant = uow.get(Participant.class, participantId);

        String id = getRequest().getAttributes().get("group").toString();
        Group group = uow.get(Group.class, id);
        try
        {
            checkPermission(group);
        } catch(AccessControlException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }

        group.addParticipant(participant);
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String participantId = getRequest().getAttributes().get("participant").toString();
        Participant participant = uow.get(Participant.class, participantId);

        String id = getRequest().getAttributes().get("group").toString();
        Group group = uow.get(Group.class, id);

        checkPermission(group);
        group.removeParticipant(participant);
    }

}