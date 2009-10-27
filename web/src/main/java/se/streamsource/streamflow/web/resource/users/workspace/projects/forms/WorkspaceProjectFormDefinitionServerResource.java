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

package se.streamsource.streamflow.web.resource.users.workspace.projects.forms;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.FormDefinition;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /workspace/projects/{project}/forms/{form}
 */
public class WorkspaceProjectFormDefinitionServerResource
        extends CommandQueryServerResource
{
    public ListValue fields() throws ResourceException
    {
        String formId = getRequest().getAttributes().get("form").toString();
        UnitOfWork uow = uowf.currentUnitOfWork();

        FormDefinition.FieldsState fields;
        try
        {
            fields = uow.get(FormDefinition.FieldsState.class, formId);
        } catch(NoSuchEntityException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
        }

        return new ListValueBuilder(vbf).addDescribableItems( fields.fields()).newList();
    }
}