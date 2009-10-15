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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to /organizations/{id}.
 */
public class OrganizationsServerResource
        extends CommandQueryServerResource
{
    @Structure
    QueryBuilderFactory qbf;


    @Override
    protected Representation get() throws ResourceException
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (form.getFirst("findbyid") != null)
        {
            // Find organizations
            String id = form.getFirstValue("id");
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                OrganizationalUnits org = uow.get(OrganizationalUnits.class, id);
                Reference orgRef = getRequest().getResourceRef().clone().addSegment(id).addSegment("");
                orgRef.setQuery("");
                getResponse().redirectPermanent(orgRef);
                return new EmptyRepresentation();
            } catch (NoSuchEntityException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
            } finally
            {
                uow.discard();
            }
        } else
        {
            return new InputRepresentation(getClass().getResourceAsStream("resources/organizationsearch.html"), MediaType.TEXT_HTML);
        }
    }


    public UserEntityListDTO users()
    {
        QueryBuilder<UserEntity> queryBuilder = qbf.newQueryBuilder(UserEntity.class);

        Property<String> username = templateFor(User.UserState.class).userName();
        Query<UserEntity> usersQuery = queryBuilder.where(
                isNotNull(username)).
                newQuery(uowf.currentUnitOfWork());

        ValueBuilder<UserEntityListDTO> listBuilder = vbf.newValueBuilder(UserEntityListDTO.class);
        List<UserEntityDTO> userlist = listBuilder.prototype().users().get();

        ValueBuilder<UserEntityDTO> builder = vbf.newValueBuilder(UserEntityDTO.class);

        for (UserEntity entity : usersQuery)
        {
            builder.prototype().entity().set(EntityReference.getEntityReference(entity));
            builder.prototype().username().set(entity.userName().get());

            userlist.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }
}
