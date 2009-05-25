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

package se.streamsource.streamflow.web.resource.organizations.projects;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.web.domain.project.Projects;
import se.streamsource.streamflow.web.domain.project.SharedProject;
import se.streamsource.streamflow.web.domain.project.SharedProjectEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/projects
 */
public class ProjectsServerResource
        extends CommandQueryServerResource
{
    public ListValue projects()
    {
        String identity = getRequest().getAttributes().get("organization").toString();
        Projects.ProjectsState projectsState = uowf.currentUnitOfWork().get(Projects.ProjectsState.class, identity);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (SharedProject project : projectsState.projects())
        {
            builder.addListItem(project.getDescription(), EntityReference.getEntityReference(project));
        }
        return builder.newList();
    }

    public void newProject(DescriptionValue value) throws DuplicateDescriptionException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<SharedProjectEntity> builder = uow.newEntityBuilder(SharedProjectEntity.class);

        String identity = getRequest().getAttributes().get("organization").toString();

        Projects projects = uow.get(Projects.class, identity);

        SharedProjectEntity projectState = builder.prototype();
        projectState.description().set(value.description().get());

        SharedProject project = builder.newInstance();

        projects.addProject(project);
    }
}