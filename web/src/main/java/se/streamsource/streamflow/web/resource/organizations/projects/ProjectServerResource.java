/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.project.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.domain.roles.Describable;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}
 */
public class ProjectServerResource
        extends CommandQueryServerResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public EntityReferenceDTO findRole(DescriptionDTO query)
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);

        try
        {
            Roles.RolesState roles = uowf.currentUnitOfWork().get(Roles.RolesState.class, getRequest().getAttributes().get("organization").toString());
            for (Role role : roles.roles())
            {
                if (role.getDescription().equals(query.description().get()))
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(role));
                }
            }
        } catch (NoSuchEntityException e)
        {
        }
        return builder.newInstance();
    }

    public void describe(DescriptionDTO descriptionValue)
    {
        String taskId = (String) getRequest().getAttributes().get("project");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);

        describable.describe(descriptionValue.description().get());
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete project"));

        String org = getRequest().getAttributes().get("organization").toString();

        Projects projects = uow.get(Projects.class, org);

        String identity = getRequest().getAttributes().get("project").toString();
        ProjectEntity projectEntity = uow.get(ProjectEntity.class, identity);

        projects.removeProject(projectEntity);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }

        return null;
    }

}