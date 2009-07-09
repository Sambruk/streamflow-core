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

package se.streamsource.streamflow.web.domain.group;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Participant.ParticipantMixin.class)
public interface Participant
        extends Identity, Describable
{
    List<Project> projects();

    abstract class ParticipantMixin
        implements Participant {

        @Structure
        Module module;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Participant participant;

        public List<Project> projects()
        {
            List<Project> result = new ArrayList<Project>();
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(ProjectEntity.class);
            queryBuilder.where(QueryExpressions.matches(
                    QueryExpressions.templateFor(ProjectEntity.class).description(), ".*"));
            Query<ProjectEntity> projects = queryBuilder.newQuery(uow);

            for (Project project: projects) {
                if (project.isMember(participant))
                {
                    result.add(project);
                }
            }

            return result;
        }
    }
}
