/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations.projects;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.project.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}
 */
public class ProjectServerResource
        extends CommandQueryServerResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public EntityReferenceValue findParticipant(DescriptionValue query)
    {
        ValueBuilder<EntityReferenceValue> builder = vbf.newValueBuilder(EntityReferenceValue.class);

        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            Participant user = uow.get(Participant.class, query.description().get());
            builder.prototype().entity().set(EntityReference.getEntityReference(user));
        } catch (NoSuchEntityException e)
        {
            // try looking up a group in the organization
            /*String org = getRequest().getAttributes().get("organization").toString();

            Groups.GroupsState groups = uow.get(Groups.GroupsState.class, org);

            for (Group group : groups.groups())
            {
                if (query.description().get().equals(group.getDescription()))
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(group));
                    return builder.newInstance();
                }
            }*/

        }
        return builder.newInstance();
    }

    public EntityReferenceValue findRole(DescriptionValue query)
    {
        ValueBuilder<EntityReferenceValue> builder = vbf.newValueBuilder(EntityReferenceValue.class);

        try
        {
            Roles.RolesState roles = uowf.currentUnitOfWork().get(Roles.RolesState.class, getRequest().getAttributes().get("organization").toString());
            for (Role role : roles.roles())
            {
                if (role.getDescription().equals(query.description().get()))
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(role));
                }
            }
        } catch (NoSuchEntityException e)
        {
        }
        return builder.newInstance();
    }


    public ListValue findParticipants(DescriptionValue query)
    {
        // TODO when query api is fixed, this must be corrected
        ValueBuilder<EntityReferenceValue> builder = vbf.newValueBuilder(EntityReferenceValue.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            Participant user = uow.get(Participant.class, query.description().get());
            builder.prototype().entity().set(EntityReference.getEntityReference(user));
            listBuilder.addListItem(user.participantDescription(), builder.newInstance().entity().get());
        } catch (NoSuchEntityException e)
        {
        }
        return listBuilder.newList();
    }



    @Override
    protected Representation delete() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete project"));

        String org = getRequest().getAttributes().get("organization").toString();

        Projects projects = uow.get(Projects.class, org);

        String identity = getRequest().getAttributes().get("project").toString();
        SharedProjectEntity projectEntity = uow.get(SharedProjectEntity.class, identity);

        projects.removeProject(projectEntity);

        uow.remove(projectEntity);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }

        return null;
    }

}