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

package se.streamsource.streamflow.web.resource.users;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.project.SharedProject;
import se.streamsource.streamflow.web.domain.project.SharedProjectEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /users/{userid}
 */
public class UserServerResource
        extends CommandQueryServerResource
{
    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        if (getRequest().getResourceRef().hasQuery())
        {
           return super.get(variant);
        }
        return getHtml("resources/user.html");
    }

    public ListValue findUsers(DescriptionDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.description().get().length() > 0)
        {
            QueryBuilder<UserEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(UserEntity.class);
            queryBuilder.where(QueryExpressions.matches(
                    QueryExpressions.templateFor(UserEntity.class).userName(), "^" + query.description().get()));
            Query<UserEntity> users = queryBuilder.newQuery();

            try
            {
                for (Participant participant : users)
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(participant));
                    listBuilder.addListItem(participant.getDescription(), builder.newInstance().entity().get());
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
            }
        }
        return listBuilder.newList();
    }


    public ListValue findGroups(DescriptionDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.description().get().length() > 0)
        {
            QueryBuilder<GroupEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(GroupEntity.class);
            queryBuilder.where(
                QueryExpressions.matches(
                    QueryExpressions.templateFor(GroupEntity.class).description(), "^" + query.description().get()));
            Query<GroupEntity> groups = queryBuilder.newQuery();

            try
            {
                for (Participant participant : groups)
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(participant));
                    listBuilder.addListItem(participant.getDescription(), builder.newInstance().entity().get());
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
            }
        }

        return listBuilder.newList();
    }

    public ListValue findProjects(DescriptionDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.description().get().length() > 0)
        {
            QueryBuilder<SharedProjectEntity> queryBuilder = uow.queryBuilderFactory().newQueryBuilder(SharedProjectEntity.class);
            queryBuilder.where(QueryExpressions.matches(
                    QueryExpressions.templateFor(SharedProjectEntity.class).description(), "^" + query.description().get()));
            Query<SharedProjectEntity> projects = queryBuilder.newQuery();

            try
            {
                for (SharedProject project : projects)
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(project));
                    listBuilder.addListItem(project.getDescription(), builder.newInstance().entity().get());
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
            }
        }

        return listBuilder.newList();
    }
}