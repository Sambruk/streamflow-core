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

package se.streamsource.streamflow.client.ui.administration.groups;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import se.streamsource.streamflow.client.ConnectionException;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.DefaultListModel;

/**
 * JAVADOC
 */
public class GroupsModel
        extends DefaultListModel
{
    @Structure
    ValueBuilderFactory vbf;

    private GroupsClientResource groups;

    public void setGroups(GroupsClientResource groupsResource)
    {
        this.groups = groupsResource;
        refresh();
    }

    public GroupsClientResource getGroups()
    {
        return groups;
    }

    public void newGroup(String description)
    {
        try
        {
            groups.post(new StringRepresentation(description));
            refresh();
        } catch (ResourceException e)
        {
            throw new ConnectionException("Could not create groups", e);
        }
    }

    public void removeGroup(String id)
    {
        try
        {
            groups.group(id).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new ConnectionException("Could not remove groups", e);
        }
    }

    private void refresh()
    {
        clear();
        try
        {
            for (ListItemValue value : groups.groups().items().get())
            {
                addElement(value);
            }
        } catch (ResourceException e)
        {
            throw new ConnectionException("Could not refresh list of groups", e);
        }
    }


}
