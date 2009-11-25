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

package se.streamsource.streamflow.client.resource.organizations.projects.forms.fields;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.resource.ResourceException;
import org.restlet.data.Reference;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;

/**
 * JAVADOC
 */
public class ProjectFormDefinitionFieldsClientResource
        extends CommandQueryClientResource
{
    public ProjectFormDefinitionFieldsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ProjectFormDefinitionFieldClientResource field(int index)
    {
        return getSubResource(""+index, ProjectFormDefinitionFieldClientResource.class);
    }

    public void addField(CreateFieldDTO createFieldDTO) throws ResourceException
    {
        putCommand("addField", createFieldDTO);
    }
}