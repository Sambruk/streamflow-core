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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FieldsView
    extends JScrollPane
{
    private JList fieldList;


    public FieldsView(@Service ApplicationContext context,
                      @Uses FieldsModel model)
    {
        JPanel panel = new JPanel(new BorderLayout());
        ActionMap am = context.getActionMap(this);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));

        model.refresh();
        fieldList = new JList(model);
        fieldList.setCellRenderer(new ListItemListCellRenderer());

        panel.add(fieldList, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);

        setViewportView(panel);
    }

    @org.jdesktop.application.Action
    public void add()
    {

    }


    @org.jdesktop.application.Action
    public void remove()
    {

    }

    public JList getFieldList()
    {
        return fieldList;
    }
}