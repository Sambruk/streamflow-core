/*
 * Copyright (c) 2009, Rickard …berg. All Rights Reserved.
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
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.organization.MoveOrganizationalUnitDTO;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /organizations/{organization}
 */
public class OrganizationServerResource
        extends CommandQueryServerResource
{
    @Structure
    ValueBuilderFactory vbf;

    public void describe(StringDTO stringValue)
    {
        String taskId = (String) getRequest().getAttributes().get("organization");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);

        checkPermission(describable);
        describable.describe(stringValue.string().get());
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        if (getRequest().getResourceRef().hasQuery())
        {
            return super.get(variant);
        }
        return getHtml("resources/organization.html");
    }

    public ListValue findUsers(StringDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.string().get().length() > 0)
        {
            QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(UserEntity.class);
            queryBuilder.where(matches(
                    templateFor(UserEntity.class).userName(), "^" + query.string().get()));
            Query<UserEntity> users = queryBuilder.newQuery(uow);

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


    public ListValue findGroups(StringDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.string().get().length() > 0)
        {
            QueryBuilder<GroupEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(GroupEntity.class);
            queryBuilder.where(
                    and(
                            eq(templateFor(GroupEntity.class).removed(), false),
                            matches(templateFor(GroupEntity.class).description(), "^" + query.string().get())));
            Query<GroupEntity> groups = queryBuilder.newQuery(uow);

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

    public ListValue findProjects(StringDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        if (query.string().get().length() > 0)
        {
            QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(ProjectEntity.class);
            queryBuilder.where(and(
                    eq(templateFor(ProjectEntity.class).removed(), false),
                    matches(templateFor(ProjectEntity.class).description(), "^" + query.string().get())));
            Query<ProjectEntity> projects = queryBuilder.newQuery(uow);

            try
            {
                for (Project project : projects)
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

    public void moveOrganizationalUnit(MoveOrganizationalUnitDTO moveValue)
    {
        String ouId = (String) getRequest().getAttributes().get("organization");
        OrganizationalUnitEntity ou = uowf.currentUnitOfWork().get(OrganizationalUnitEntity.class, ouId);
        OrganizationalUnit fromEntity = uowf.currentUnitOfWork().get(OrganizationalUnit.class, moveValue.from().get().identity());
        OrganizationalUnit toEntity = uowf.currentUnitOfWork().get(OrganizationalUnit.class, moveValue.to().get().identity());

        checkPermission(ou);

        ou.moveOrganizationalUnit(fromEntity, toEntity);
    }
}