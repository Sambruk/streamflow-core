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

package se.streamsource.streamflow.web.resource.organizations;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import se.streamsource.streamflow.web.resource.BaseServerResource;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;

/**
 * Mapped to /organizations/{organization}
 */
public class OrganizationServerResource
        extends CommandQueryServerResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        if (getRequest().getResourceRef().hasQuery())
        {
           return super.get(variant);
        }
        return get();    
    }

    @Override
    protected Representation get() throws ResourceException
    {
        return getHtml("resources/organization.html");
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

}