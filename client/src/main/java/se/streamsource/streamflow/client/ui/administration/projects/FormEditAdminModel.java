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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldClientResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class FormEditAdminModel
    implements Refreshable, EventListener, EventHandler

{

    @Structure
    ValueBuilderFactory vbf;

    EventSourceListener subscriber;
    EventSource source;

    private EventHandlerFilter eventFilter;
    private ProjectFormDefinitionClientResource resource;
    private FormValue formValue;

    public FormEditAdminModel(@Uses ProjectFormDefinitionClientResource resource)
    {
        eventFilter = new EventHandlerFilter( this, "addedForm", "removedForm");
        this.resource = resource;
        refresh();
    }

    public void refresh() throws OperationException
    {
        try
        {
            formValue = resource.form();
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }

    }

    public void notifyEvent(DomainEvent event)
    {

    }

    public boolean handleEvent(DomainEvent event)
    {
        return false;
    }

    public FormValue getFormValue()
    {
        return formValue;
    }

    public void changeDescription(StringDTO description) throws ResourceException
    {
        resource.changeDescription(description);
    }

    public void changeNote(StringDTO stringDTO) throws ResourceException
    {
        resource.changeNote(stringDTO);
    }

    public ProjectFormDefinitionFieldClientResource getFieldResource(int idx)
    {
        return resource.fields().field(idx);
    }
}