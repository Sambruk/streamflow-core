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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormsView
        extends JPanel
{
    public JList formList;
    private FormsModel model;

    public FormsView(@Service ApplicationContext context, @Uses FormsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        formList = new JList(model);

        formList.setCellRenderer(new ListItemCellRenderer());

        add(formList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        formList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
    }

    @Action
    public void add()
    {
    }

    @Action
    public void remove()
    {
    }

    public JList getFormList()
    {
        return formList;
    }
}