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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.task.TaskFormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import java.util.List;

/**
 * Model for a FormDefinition
 */
public class FormSubmitModel
{
    private List<ListItemValue> fieldValues;
    private EntityReference formEntityReference;

    @Uses
    TaskSubmittedFormsClientResource submittedFormsResource;

    public FormSubmitModel(@Uses TaskFormDefinitionClientResource form)
    {
        try
        {
            fieldValues = form.fields().items().get();
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_get_form, e);
        }
        formEntityReference = form.formEntityReference();
    }

    public List<ListItemValue> fieldsForPage(String pageId)
    {
        // find field in page with id
        return fieldValues;
    }

    public List<ListItemValue> fields()
    {
        return fieldValues;
    }

    public EntityReference formEntityReference()
    {
        return formEntityReference;
    }

    public void submit(SubmitFormDTO submitFormDTO)
    {
        try
        {
            submittedFormsResource.submitForm(submitFormDTO);
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_submit_form, e);
        }
    }
}