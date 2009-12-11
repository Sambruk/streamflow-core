/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskSubmittedFormsClientResource;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.AbstractListModel;
import java.util.List;

public class FormsListModel
    extends AbstractListModel
    implements Refreshable
{
    private List<ListItemValue> forms;

    @Uses
    TaskSubmittedFormsClientResource resource;

    public void refresh() throws OperationException
    {
        try
        {
            forms = resource.applicableFormDefinitionList().items().get();
            fireContentsChanged(this, 0, forms.size());
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_get_submitted_form, e);
        }
    }

    public int getSize()
    {
        return forms.size();
    }

    public Object getElementAt(int i)
    {
        return forms.get(i);
    }
}
