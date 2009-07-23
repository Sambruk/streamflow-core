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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.resource.label.LabelDTO;
import se.streamsource.streamflow.resource.label.LabelListDTO;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * JAVADOC
 */
public class LabelsModel
    extends AbstractListModel
    implements ComboBoxModel
{
    private LabelsClientResource labels;
    private LabelListDTO list;

    private LabelDTO selected;

    public LabelsModel(@Uses LabelsClientResource labels)
    {
        this.labels = labels;
    }

    public void setSelectedItem(Object anItem)
    {
        selected = (LabelDTO) anItem;
    }

    public LabelDTO getSelectedItem()
    {
        return selected;
    }

    public int getSize()
    {
        return list == null ? 0 : list.labels().get().size();
    }

    public LabelDTO getElementAt(int index)
    {
        return list.labels().get().get(index);
    }

    public void refresh() throws ResourceException
    {
        if (list != null)
            fireIntervalRemoved(this, 0, list.labels().get().size());

        // Get label list
        list = labels.labels();

        fireIntervalAdded(this, 0, list.labels().get().size());
    }
}
