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

package se.streamsource.streamflow.web.resource.users.overview;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.property.Property;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to /user/{userid}/overview
 */
public class OverviewServerResource
        extends CommandQueryServerResource
{
    @Structure
    protected ObjectBuilderFactory obf;

    @Override
    protected Representation get() throws ResourceException
    {
        return getHtml("resources/overview.html");
    }

    public Representation get(Variant variant)
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Get project overview summary"));

        ValueBuilder<ProjectSummaryDTO> builder = vbf.newValueBuilder(ProjectSummaryDTO.class);
        ProjectSummaryDTO builderPrototype = builder.prototype();

        ValueBuilder<ProjectSummaryListDTO> listBuilder = vbf.newValueBuilder(ProjectSummaryListDTO.class);
        ProjectSummaryListDTO listBuilderPrototype = listBuilder.prototype();


        String id = (String) getRequest().getAttributes().get("user");
        Participant.ParticipantState participant = uow.get(Participant.ParticipantState.class, id);

        for (Project project : participant.allProjects())
        {
            QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            Association<Assignee> assigneeAssociation = templateFor(Assignable.AssignableState.class).assignedTo();
            Property<String> ownableId = templateFor(Ownable.OwnableState.class).owner().get().identity();

            queryBuilder.where(and(
                 eq(ownableId, ((ProjectEntity)project).identity().get()),
                isNull(assigneeAssociation),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));
            Query<TaskEntity> inboxQuery = queryBuilder.newQuery(uow);

            queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            queryBuilder.where(and(
                    eq(ownableId, ((ProjectEntity)project).identity().get()),
                    isNotNull(assigneeAssociation),
                    eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));
            Query<TaskEntity> assignedQuery = queryBuilder.newQuery(uow);

            queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            queryBuilder.where(and(
                    eq(ownableId, ((ProjectEntity)project).identity().get()),
                    eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));
            Query<TaskEntity> totalQuery = queryBuilder.newQuery(uow);

            builderPrototype.project().set(project.getDescription());
            builderPrototype.inboxCount().set(new Long(inboxQuery.count()).intValue());
            builderPrototype.assignedCount().set(new Long(assignedQuery.count()).intValue());
            builderPrototype.totalActive().set(new Long(totalQuery.count()).intValue());

            listBuilderPrototype.projectOverviews().get().add(builder.newInstance());

        }
        return new StringRepresentation(listBuilder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
    }
}