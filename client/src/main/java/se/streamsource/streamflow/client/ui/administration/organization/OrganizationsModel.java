/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

public class OrganizationsModel
        extends AbstractListModel
        implements EventListener, EventHandler
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    private EventHandlerFilter eventFilter = new EventHandlerFilter(this, "createdOrganization", "createdUser");

    WeakModelMap<String, OrganizationUsersModel> organizationUsersModels = new WeakModelMap<String, OrganizationUsersModel>()
    {
        @Override
        protected OrganizationUsersModel newModel(String key)
        {
            OrganizationUsersModel model;
            try
            {
                model = obf.newObjectBuilder(OrganizationUsersModel.class)
                        .use(organizationsResource.organization(key), organizationsResource).newInstance();
            } catch (ResourceException e)
            {
                throw new OperationException(AdministrationResources.could_not_find_organization,e);
            }
            return model;
        }
    };

    private OrganizationsClientResource organizationsResource;

    private List<ListItemValue> organizations;

    public OrganizationsModel(@Uses OrganizationsClientResource organizationsResource)
    {
        this.organizationsResource = organizationsResource;
        this.refresh();
    }

    public int getSize()
    {
        return organizations == null ? 0 : organizations.size();
    }

    public Object getElementAt(int index)
    {
        return organizations == null ? null : organizations.get(index);
    }

    public void refresh()
    {
        try
        {
            organizations = organizationsResource.organizations().items().get();

            fireContentsChanged(this, 0, organizations.size());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh, e);
        }
    }


    public OrganizationUsersModel getOrganizationUsersModel(String id)
    {
        return organizationUsersModels.get(id);
    }

    public void notifyEvent(DomainEvent event)
    {
        eventFilter.handleEvent(event);

        for (OrganizationUsersModel model : organizationUsersModels)
        {
            model.notifyEvent(event);
        }
    }

    public boolean handleEvent(DomainEvent event)
    {
        Logger.getLogger("administration").info("Refresh organizations");
        refresh();
        return false;
    }
}
