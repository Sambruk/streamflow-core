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
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;

/**
 * JAVADOC
 */
@Mixins(Projects.ProjectsMixin.class)
public interface Projects
{
    ProjectEntity createProject(String name);

    boolean removeProject(Project project);

    interface ProjectsState
    {
        @Aggregated
        ManyAssociation<Project> projects();

        ProjectEntity projectCreated(DomainEvent event, String id);
        void projectRemoved(DomainEvent event, Project project);
    }

    abstract class ProjectsMixin
            implements Projects, ProjectsState
    {
        @This
        OrganizationalUnit ou;

        @Service
        IdentityGenerator idgen;

        @Structure
        UnitOfWorkFactory uowf;

        public ProjectEntity createProject(String name)
        {
            String id = idgen.generate(ProjectEntity.class);

            ProjectEntity project = projectCreated(DomainEvent.CREATE, id);
            project.describe(name);

            return project;
        }

        public ProjectEntity projectCreated(DomainEvent event, String id)
        {
            EntityBuilder<ProjectEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(ProjectEntity.class, id);
            builder.instance().organizationalUnit().set(ou);
            ProjectEntity project = builder.newInstance();

            projects().add(project);

            return project;
        }
    }


}