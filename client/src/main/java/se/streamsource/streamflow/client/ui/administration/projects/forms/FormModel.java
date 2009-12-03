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
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.fields.ProjectFormDefinitionFieldsClientResource;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.roles.StringDTO;

import java.util.Observable;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FormModel
    extends Observable
    implements Refreshable, EventListener, EventHandler

{
    @Structure
    ObjectBuilderFactory obf;

    private EventHandlerFilter eventFilter;

    private ProjectFormDefinitionClientResource resource;
    private FormValue formValue;

    public FormModel(@Uses ProjectFormDefinitionClientResource resource)
    {
        eventFilter = new EventHandlerFilter( this, "changedNote", "movedField", "changedDescription");
        this.resource = resource;
        refresh();
    }

    WeakModelMap<String, FieldsModel> fieldsModels = new WeakModelMap<String, FieldsModel>()
    {

        protected FieldsModel newModel(String key)
        {
            ProjectFormDefinitionFieldsClientResource fieldsResource = resource.fields();
            return obf.newObjectBuilder(FieldsModel.class).use(fieldsResource).newInstance();
        }
    };


    public void refresh() throws OperationException
    {
        try
        {
            formValue = resource.form();
            setChanged();
            notifyObservers(this);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }

    }

    public void notifyEvent(DomainEvent event)
    {
        eventFilter.handleEvent( event );
        for (FieldsModel fieldsModel : fieldsModels)
        {
            fieldsModel.notifyEvent( event );
        }

    }

    public boolean handleEvent(DomainEvent event)
    {
        if (formValue.form().get().identity().equals(event.entity().get()))
        {
            if (event.name().get().equals("movedField"))
            {
                getFieldsModel().refresh();
            }
            Logger.getLogger("administration").info("Refresh the note");
            refresh();
        }
        return false;
    }

    public String getNote()
    {
        return formValue.note().get();
    }

    public FormValue getFormValue()
    {
        return formValue;
    }

    public ProjectFormDefinitionClientResource getResource()
    {
        return resource;
    }

    public void changeDescription(StringDTO description) throws ResourceException
    {
        resource.changeDescription(description);
    }

    public void changeNote(StringDTO stringDTO) throws ResourceException
    {
        resource.changeNote(stringDTO);
    }

    public FieldsModel getFieldsModel()
    {
        return fieldsModels.get(formValue.form().get().identity());
    }
}