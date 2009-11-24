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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;

import javax.swing.*;
import java.util.List;

/**
 * JAVADOC
 */
public class FieldModel
    extends AbstractListModel
        implements Refreshable
{
    @Uses
    ProjectFormDefinitionClientResource formResource;

    @Structure
    ValueBuilderFactory vbf;

    EventSourceListener subscriber;
    EventSource source;

    private List<ListItemValue> fieldsList;

    public int getSize()
    {
        return fieldsList == null ? 0 : fieldsList.size();
    }

    public Object getElementAt( int index )
    {
        return fieldsList.get( index );
    }

    public void refresh()
    {
        try
        {
            fieldsList = formResource.form().fields().get().items().get();
            fireContentsChanged( this, 0, getSize() );
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_members, e);
        }
    }

    public void addField(EntityReference field)
    {
        /*
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(form);
        try
        {
            formResource.fields().addField(builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_form_definition, e);
        }*/
    }

    public void removeField(EntityReference field)
    {
        /*
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(form);
        try
        {
            forms.removeForm(builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_form_definition, e);
        }
        */
    }

    public List<ListItemValue> getProjectFormFieldList()
    {
        return fieldsList;
    }

    public ProjectFormDefinitionClientResource getFormsResource()
    {
        return formResource;
    }
}