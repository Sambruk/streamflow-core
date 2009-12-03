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

package se.streamsource.streamflow.client.ui.administration.projects.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldsClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FieldsModel
    extends AbstractListModel
        implements Refreshable, EventListener, EventHandler
{
    @Uses
    ProjectFormDefinitionFieldsClientResource fieldsResource;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;


    WeakModelMap<String, FieldValueEditModel> fieldModels = new WeakModelMap<String, FieldValueEditModel>()
    {
        protected FieldValueEditModel newModel( String key )
        {
            try
            {
                ListValue value = fieldsResource.fields();
                int index = 0;
                for (ListItemValue listItemValue : value.items().get())
                {
                    if (listItemValue.entity().get().identity().equals(key))
                    {
                        break;
                    }
                    index++;
                }
                ProjectFormDefinitionFieldClientResource fieldResource = fieldsResource.field(index);
                return obf.newObjectBuilder(FieldValueEditModel.class).use(fieldResource).newInstance();
            } catch (ResourceException e)
            {
                throw new OperationException(AdministrationResources.could_not_get_form, e);
            }
        }
    };

    EventHandlerFilter eventFilter = new EventHandlerFilter(this, "changedDescription" , "movedField" );

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
            fieldsList = fieldsResource.fields().items().get();
            fireContentsChanged( this, 0, getSize() );
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_members, e);
        }
    }

    public void addField(String name, FieldTypes fieldType)
    {

        ValueBuilder<CreateFieldDTO> builder = vbf.newValueBuilder(CreateFieldDTO.class);
        builder.prototype().name().set(name);
        builder.prototype().fieldType().set(fieldType);

        try
        {
            fieldsResource.addField(builder.newInstance());
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_field, e);
        }
    }

    public void removeField(int index)
    {
        try
        {
            fieldsResource.field(index).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_field, e);
        }
    }

    public void moveField(int fromIndex, int newIndex)
    {
        ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder(IntegerDTO.class);
        builder.prototype().integer().set(newIndex);
        try
        {
            fieldsResource.field(fromIndex).moveField(builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_field, e);
        }
    }

    public void notifyEvent( DomainEvent event )
    {
        eventFilter.handleEvent( event );
        for (FieldValueEditModel fieldModel : fieldModels)
        {
            fieldModel.notifyEvent( event );
        }

    }

    public boolean handleEvent( DomainEvent event )
    {
        for (ListItemValue value : fieldsList)
        {
            if (event.name().get().equals("movedField"))
            {
                if ( event.parameters().get().contains( value.entity().get().identity() ))
                {
                    fieldModels.clear();
                }
            }

            if ( event.entity().get().equals( value.entity().get().identity() ))
            {
                Logger.getLogger("adminitration").info("Refresh field list");
                refresh();
            }
        }

        return false;
    }

    public FieldValueEditModel getFieldModel( String id)
    {
        return fieldModels.get(id);
    }
}