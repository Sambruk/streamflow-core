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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskGeneralClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.*;
import java.util.List;

public class LabelSelectionModel
    extends AbstractListModel
    implements ComboBoxModel, Refreshable

{
    @Structure
    ValueBuilderFactory vbf;

    @Uses
    TaskGeneralClientResource resource;
    private String prefix = "";

    List<ListItemValue> possibleLabels;

    ListItemValue selectedItem;

    public int getSize()
    {
        return possibleLabels == null ? 0 : possibleLabels.size();
    }

    public Object getElementAt(int index)
    {
        return possibleLabels == null ? null : possibleLabels.get(index);
    }

    public void setSelectedItem(Object anItem)
    {
        selectedItem = (ListItemValue) anItem;
    }

    public Object getSelectedItem()
    {
        return selectedItem;
    }

    public void setLabelPrefix(String newPrefix)
    {
        if (prefix.equals(""))
        {
            this.prefix = newPrefix;
            refresh();
        } else
            this.prefix = newPrefix;
    }

    public void refresh() throws OperationException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(prefix);
        try
        {
            possibleLabels = resource.possibleLabels(builder.newInstance()).items().get();
            fireContentsChanged(this, 0, possibleLabels.size());
        } catch (ResourceException e)
        {
            throw new OperationException(TaskResources.could_not_refresh, e);
        }
    }
}
