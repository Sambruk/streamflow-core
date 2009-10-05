/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.group;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.domain.task.TaskStates;

@Mixins(ParticipantQueries.ParticipantQueriesMixin.class)
public interface ParticipantQueries
{

    public ProjectSummaryListDTO getProjectsSummary();

    class ParticipantQueriesMixin
        implements ParticipantQueries
    {
        @Structure
        QueryBuilderFactory qbf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Identity id;

        @This
        Participant.ParticipantState participant;

        public ProjectSummaryListDTO getProjectsSummary()
        {
            UnitOfWork uow =  uowf.currentUnitOfWork();

            ValueBuilder<ProjectSummaryDTO> builder = vbf.newValueBuilder(ProjectSummaryDTO.class);
            ProjectSummaryDTO builderPrototype = builder.prototype();

            ValueBuilder<ProjectSummaryListDTO> listBuilder = vbf.newValueBuilder(ProjectSummaryListDTO.class);
            ProjectSummaryListDTO listBuilderPrototype = listBuilder.prototype();

            for (Project project : participant.allProjects())
            {
                Association<Assignee> assigneeAssociation = templateFor(Assignable.AssignableState.class).assignedTo();
                Property<String> ownableId = templateFor(Ownable.OwnableState.class).owner().get().identity();

                QueryBuilder<TaskEntity> ownerQueryBuilder = qbf.newQueryBuilder(TaskEntity.class).where(
                        eq(ownableId, ((ProjectEntity)project).identity().get()));

                QueryBuilder<TaskEntity> inboxQueryBuilder = ownerQueryBuilder.where(and(
                    isNull(assigneeAssociation),
                    eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));
                Query<TaskEntity> inboxQuery = inboxQueryBuilder.newQuery(uow);

                
                QueryBuilder<TaskEntity> assignedQueryBuilder = ownerQueryBuilder.where(and(
                        isNotNull(assigneeAssociation),
                        eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));
                Query<TaskEntity> assignedQuery = assignedQueryBuilder.newQuery(uow);

                builderPrototype.project().set(project.getDescription());
                builderPrototype.inboxCount().set(inboxQuery.count());
                builderPrototype.assignedCount().set(assignedQuery.count());

                listBuilderPrototype.projectOverviews().get().add(builder.newInstance());

            }
            return listBuilder.newInstance();
        }
    }
}
