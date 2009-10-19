/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.resource.organizations.projects;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class ProjectClientResource
        extends CommandQueryClientResource
{
    public ProjectClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public MembersClientResource members()
    {
        return getSubResource("members", MembersClientResource.class);
    }

    public LabelsClientResource labels()
    {
        return getSubResource("labels", LabelsClientResource.class);
    }

    public FormDefinitionsClientResource forms()
    {
        return getSubResource("forms", FormDefinitionsClientResource.class);
    }

    public void describe(StringDTO stringValue) throws ResourceException
    {
        putCommand("describe", stringValue);
    }
}