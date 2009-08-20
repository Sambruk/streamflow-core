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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.domain.project.ProjectStates;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;

/**
 * JAVADOC
 */
@Mixins(Projects.ProjectsMixin.class)
public interface Projects
{
    Project createProject(String name) throws DuplicateDescriptionException;

    void removeProject(Project project);

    void completeProject(Project project);

    interface ProjectsState
    {
        @Aggregated
        ManyAssociation<Project> projects();

        ManyAssociation<Project> active();

        ManyAssociation<Project> completed();

        ManyAssociation<Project> dropped();
    }

    class ProjectsMixin
            implements Projects
    {
        @This
        ProjectsState state;

        @This
        OrganizationalUnit ou;

        @Structure
        UnitOfWorkFactory uowf;

        public Project createProject(String name) throws DuplicateDescriptionException
        {
            for (Project aProject : state.projects())
            {
                if (aProject.hasDescription(name))
                {
                    throw new DuplicateDescriptionException();
                }
            }

            EntityBuilder<ProjectEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(ProjectEntity.class);
            builder.instance().describe(name);
            builder.instance().organizationalUnit().set(ou);
            ProjectEntity project = builder.newInstance();

            state.projects().add(state.projects().count(), project);
            state.active().add(state.active().count(), project);

            return project;
        }

        public void removeProject(Project project)
        {
            if (state.projects().remove(project))
            {
                if (project.getStatus().equals(ProjectStates.ACTIVE))
                {
                    state.active().remove(project);
                } else if (project.getStatus().equals(ProjectStates.COMPLETED))
                {
                    state.completed().remove(project);
                } else if (project.getStatus().equals(ProjectStates.DROPPED))
                {
                    state.dropped().remove(project);
                }

                project.remove();
            }
        }

        public void completeProject(Project project)
        {
            if (project.complete())
            {
                state.active().remove(project);
                state.completed().add(state.completed().count(), project);
            }
        }
    }


}