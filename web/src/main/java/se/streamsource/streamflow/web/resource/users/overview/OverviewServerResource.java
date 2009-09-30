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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.usecase.UsecaseBuilder;
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
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

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
        ValueBuilder<ProjectSummaryListDTO> listBuilder = vbf.newValueBuilder(ProjectSummaryListDTO.class);

        String id = (String) getRequest().getAttributes().get("user");
        Participant.ParticipantState participant = uow.get(Participant.ParticipantState.class, id);

        for (Project project : participant.allProjects())
        {
            // Find all Active tasks delegated to "project" that have not yet been assigned
            QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            Association<Delegatee> delegatedTo = templateFor(Delegatable.DelegatableState.class).delegatedTo();

            Association<Assignee> assigneeAssociation = templateFor(Assignable.AssignableState.class).assignedTo();
            queryBuilder.where(and(
                eq(delegatedTo, uow.get(Delegatee.class, id)),
                isNull(assigneeAssociation),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

            Query<TaskEntity> openQuery = queryBuilder.newQuery(uow);

            buildList(openQuery, project, builder, listBuilder);


        }
        return new StringRepresentation(listBuilder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
    }

    private <T extends ProjectSummaryListDTO> void buildList(Query<TaskEntity> openQuery, Project project, ValueBuilder<ProjectSummaryDTO> builder, ValueBuilder<ProjectSummaryListDTO> listBuilder)
    {
        ProjectSummaryDTO prototype = builder.prototype();

        prototype.project().set(project.getDescription());

        prototype.inboxCount().set(new Long(openQuery.count()).intValue());
        prototype.assignedCount().set(new Long(openQuery.count()).intValue());

        List<ProjectSummaryDTO> list = (List<ProjectSummaryDTO>)listBuilder.prototype().projectOverviews().get();
        list.add(builder.newInstance());

    }
}