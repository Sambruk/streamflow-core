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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.AbstractListModel;
import java.util.List;

/**
 * JAVADOC
 */
public class GroupsModel
        extends AbstractListModel
        implements Refreshable
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    WeakModelMap<String, GroupModel> groupModels = new WeakModelMap<String, GroupModel>()
    {
        @Override
        protected GroupModel newModel(String key)
        {
            return obf.newObjectBuilder(GroupModel.class).use(groupsResource.group(key)).newInstance();
        }
    };

    @Uses
    private GroupsClientResource groupsResource;

    private List<ListItemValue> groups;

    public void newGroup(String description)
    {
        try
        {
            groupsResource.post(new StringRepresentation(description));
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_create_group, e);
        }
    }

    public void removeGroup(String id)
    {
        try
        {
            groupsResource.group(id).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_group, e);
        }
    }

    public int getSize()
    {
        return groups == null ? 0 : groups.size();
    }

    public Object getElementAt(int index)
    {
        return groups == null ? null : groups.get(index);
    }

    public void refresh() throws ResourceException
    {
        // Get label list
        groups = groupsResource.groups().items().get();

        fireContentsChanged(this, 0, groups.size());
    }


    public GroupModel getGroupModel(String id)
    {
        return groupModels.get(id);
    }

    public void describe(int selectedIndex, String newName) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(newName);

        groupsResource.group(groups.get(selectedIndex).entity().get().identity()).describe(builder.newInstance());

        refresh();
    }
}
