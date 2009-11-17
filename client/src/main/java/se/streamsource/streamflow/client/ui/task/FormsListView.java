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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.resource.task.TaskFormDefinitionsClientResource;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormsListView
        extends JScrollPane
{
    public JList getFormList()
    {
        return formList;
    }

    private JList formList;
    FormsListModel model;

    public FormsListView(@Service ApplicationContext context,
                         @Uses FormsListModel model)
    {
        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        setMinimumSize(new Dimension(150, 0));
        this.model = model;

        JPanel panel = new JPanel(new BorderLayout());
        formList = new JList(model);
        formList.setCellRenderer(new ListItemCellRenderer());
        panel.add(formList, BorderLayout.CENTER);
        setViewportView(panel);
    }

    public TaskFormDefinitionsClientResource getResource()
    {
        return model.getResource();
    }
}