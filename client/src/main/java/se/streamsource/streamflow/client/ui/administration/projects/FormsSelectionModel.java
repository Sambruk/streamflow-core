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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.util.List;

public class FormsSelectionModel
        extends AbstractListModel
{
    List<ListItemValue> forms;

    public FormsSelectionModel(
            //@Uses OrganizationalUnitAdministrationModel organizationModel
            @Uses ProjectFormDefinitionsClientResource formsResource
    )
    {
        try
        {
            forms = formsResource.nonApplicableForms().items().get();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_list_form_definitions, e);
        }
    }

    public int getSize()
    {
        return forms==null ? 0 : forms.size();
    }

    public Object getElementAt(int i)
    {
        return forms==null ? "" : forms.get(i);
    }
}