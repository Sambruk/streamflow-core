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

package se.streamsource.streamflow.client.ui.administration.roles;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.roles.RolesClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.util.List;

/**
 * JAVADOC
 */
public class RolesModel
        extends AbstractListModel
{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    private RolesClientResource roles;

    private List<ListItemValue> list;

    public int getSize()
    {
        return list == null ? 0 : list.size();
    }

    public Object getElementAt(int index)
    {
        return list == null ? null : list.get(index);
    }

    public void newRole(String description)
    {
        try
        {
            roles.post(new StringRepresentation(description));
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_create_role, e);
        }

    }

    public void removeRole(String id)
    {
        try
        {
            roles.role(id).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_role, e);
        }
    }

    public void refresh()
    {
        try
        {
            list = roles.roles().items().get();
            fireContentsChanged(this, 0, list.size());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_roles, e);
        }
    }

}