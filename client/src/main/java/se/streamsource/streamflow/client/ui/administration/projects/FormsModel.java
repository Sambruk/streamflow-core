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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.FormDefinitionsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.OnEvents;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FormsModel
    extends AbstractListModel
        implements Refreshable

{
    @Uses
    FormDefinitionsClientResource forms;

    @Structure
    ValueBuilderFactory vbf;

    EventSourceListener subscriber;
    EventSource source;

    private List<ListItemValue> formsList;

    public FormsModel(@Service EventSource source)
    {
        subscriber = new OnEvents("projectFormDefinitionAdded", "projectFormDefinitionRemoved")
        {
            public void run()
            {
                Logger.getLogger("administration").info("Refresh project form definitions");
                refresh();
            }
        };

        this.source = source;
        source.registerListener(subscriber);
    }

    public int getSize()
    {
        return formsList == null ? 0 : formsList.size();
    }

    public Object getElementAt( int index )
    {
        return formsList.get( index );
    }

    public void refresh()
    {
        try
        {
            formsList = forms.forms().items().get();
            fireContentsChanged( this, 0, getSize() );
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_members, e);
        }
    }

    public void addForm(EntityReference form)
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(form);
        try
        {
            forms.addForm(builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_form_definition, e);
        }
    }

    public void removeForm(EntityReference form)
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(form);
        try
        {
            forms.removeForm(builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_form_definition, e);
        }
    }
}