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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.entity.EntityReference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskFormDefinitionClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import java.util.List;
import java.util.Observable;

/**
 * Model for a FormDefinition
 */
public class FormSubmitModel
    extends Observable
    implements Refreshable
{
    @Uses
    TaskFormDefinitionClientResource form;

    private List<ListItemValue> fieldValues;

    public void refresh() throws OperationException
    {
        try
        {
            fieldValues = form.fields().items().get();
            setChanged();
            notifyObservers();
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_get_form, e);
        }
    }

    public List<ListItemValue> fields()
    {
        return fieldValues;
    }

    public EntityReference formEntityReference()
    {
        return form.formEntityReference();
    }
}