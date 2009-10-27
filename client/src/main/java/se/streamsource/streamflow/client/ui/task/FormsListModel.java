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
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.client.resource.users.workspace.projects.forms.WorkspaceProjectFormDefinitionsClientResource;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.*;
import java.util.List;

public class FormsListModel
    extends AbstractListModel
{
    List<ListItemValue> forms;

    public FormsListModel(@Uses WorkspaceProjectFormDefinitionsClientResource resource)
    {
        try
        {
            forms = resource.applicableFormDefinitionList().items().get();
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
