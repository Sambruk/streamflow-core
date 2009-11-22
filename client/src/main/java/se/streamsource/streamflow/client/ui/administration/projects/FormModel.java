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
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FormValue;

/**
 * JAVADOC
 */
public class FormModel
{

    @Uses
    ProjectFormDefinitionClientResource projectForm;

    private FormValue formValue;


    public void refresh()
    {
        try
        {
            formValue = projectForm.form();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_get_form, e);
        }
    }

    public String getDescription()
    {
        return formValue.description().get();
    }

    public String getNote()
    {
        return formValue.note().get();
    }

}
