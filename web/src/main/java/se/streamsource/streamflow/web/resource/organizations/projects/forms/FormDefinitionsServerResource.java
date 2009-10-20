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

package se.streamsource.streamflow.web.resource.organizations.projects.forms;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.form.FormDefinition;
import se.streamsource.streamflow.web.domain.project.ProjectFormDefinitions;
import se.streamsource.streamflow.web.domain.project.ProjectFormDefinitionsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/forms
 */
public class FormDefinitionsServerResource
        extends CommandQueryServerResource
{
    public ListValue forms()
    {
        String identity = getRequest().getAttributes().get("project").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        ProjectFormDefinitionsQueries forms = uow.get(ProjectFormDefinitionsQueries.class, identity);

        return forms.formDefinitionList();
    }

    public void addForm(EntityReferenceDTO formReference) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("project").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormDefinition formDefinition;
        try
        {
            formDefinition = uow.get(FormDefinition.class, formReference.entity().get().identity());
        } catch(NoSuchEntityException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        }

        ProjectFormDefinitions forms = uow.get(ProjectFormDefinitions.class, identity);

        forms.addFormDefinition(formDefinition);
    }

    public void removeForm(EntityReferenceDTO formReference) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("project").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormDefinition formDefinition;
        try
        {
            formDefinition = uow.get(FormDefinition.class, formReference.entity().get().identity());
        } catch(NoSuchEntityException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        }

        ProjectFormDefinitions forms = uow.get(ProjectFormDefinitions.class, identity);

        forms.removeFormDefinition(formDefinition);
    }

}