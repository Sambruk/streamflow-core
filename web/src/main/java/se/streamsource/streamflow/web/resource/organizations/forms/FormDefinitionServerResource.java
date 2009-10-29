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

package se.streamsource.streamflow.web.resource.organizations.forms;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.Fields;
import se.streamsource.streamflow.web.domain.form.FormDefinitionEntity;
import se.streamsource.streamflow.web.domain.form.FormDefinitions;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/forms/{form}
 *
 * AND
 *
 * /tasks/{task}/formDefinitions/{form}
 */
public class FormDefinitionServerResource
        extends CommandQueryServerResource
{
    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String identity = getRequest().getAttributes().get("organization").toString();
        OrganizationalUnitRefactoring.Data ou = uowf.currentUnitOfWork().get( OrganizationalUnitRefactoring.Data.class, identity);

        FormDefinitions forms = ou.organization().get();
        checkPermission( forms );

        String formId = getRequest().getAttributes().get("form").toString();
        FormDefinitionEntity formDefinitionEntity = uow.get(FormDefinitionEntity.class, formId);

        forms.removeFormDefinition( formDefinitionEntity);
    }

    public ListValue fields() throws ResourceException
    {
        String formId = getRequest().getAttributes().get("form").toString();
        UnitOfWork uow = uowf.currentUnitOfWork();

        Fields.Data fields;
        try
        {
            fields = uow.get(Fields.Data.class, formId);
        } catch(NoSuchEntityException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        }

        return new ListValueBuilder(vbf).addDescribableItems( fields.fields()).newList();
    }



}