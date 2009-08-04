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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * JAVADOC
 */
public class LabelsModel
    extends AbstractListModel
    implements ComboBoxModel
{
    @Structure
    ValueBuilderFactory vbf;

    private LabelsClientResource labels;
    private ListValue list;

    private ListItemValue selected;

    public LabelsModel(@Uses LabelsClientResource labels)
    {
        this.labels = labels;
    }

    public void setSelectedItem(Object anItem)
    {
        selected = (ListItemValue) anItem;
    }

    public ListItemValue getSelectedItem()
    {
        return selected;
    }

    public int getSize()
    {
        return list == null ? 0 : list.items().get().size();
    }

    public ListItemValue getElementAt(int index)
    {
        return list.items().get().get(index);
    }

    public void refresh()
    {
        try
        {
            if (list != null)
                fireIntervalRemoved(this, 0, list.items().get().size());

            // Get label list
            list = labels.labels();

            fireIntervalAdded(this, 0, list.items().get().size());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_labels, e);
        }
    }

    public void newLabel(String description)
    {
        try
        {
            labels.post(new StringRepresentation(description));
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_create_label, e);
        }
    }

    public void describe(int selectedIndex, String name) throws ResourceException
    {
        ValueBuilder<DescriptionDTO> builder = vbf.newValueBuilder(DescriptionDTO.class);
        builder.prototype().description().set(name);

        labels.label(list.items().get().get(selectedIndex).entity().get().identity()).describe(builder.newInstance());

        refresh();
    }
}
