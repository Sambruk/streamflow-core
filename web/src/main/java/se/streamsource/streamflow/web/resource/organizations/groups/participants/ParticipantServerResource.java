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
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.injection.scope.Structure;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.group.*;
import se.streamsource.streamflow.web.resource.BaseServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/groups/{group}/participants/{participant}
 */
public class ParticipantServerResource
        extends BaseServerResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    //TODO do a check if the orgunit actually contains the project
    @Override
    protected Representation put(Representation representation) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Add participant"));

        String participantId = getRequest().getAttributes().get("participant").toString();
        Participant participant = uow.get(Participant.class, participantId);

        String id = getRequest().getAttributes().get("group").toString();
        Group group = uow.get(Group.class, id);

        group.addParticipant(participant);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
            throw new ResourceException(e);
        }

        return null;
    }

    @Override
    protected Representation delete() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Remove participant"));

        String participantId = getRequest().getAttributes().get("participant").toString();
        Participant participant = uow.get(Participant.class, participantId);

        String id = getRequest().getAttributes().get("group").toString();
        Group group = uow.get(Group.class, id);

        group.removeParticipant(participant);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
            throw new ResourceException(e);
        }

        return null;

    }

}