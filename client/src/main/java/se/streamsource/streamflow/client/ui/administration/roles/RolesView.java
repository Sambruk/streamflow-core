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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class RolesView
        extends JPanel
{
    RolesModel model;

    @Service
    DialogService dialogs;

    @Structure
    UnitOfWorkFactory uowf;
    public JList projectList;

    public RolesView(@Service ActionMap am, @Service final RolesModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(am);

        projectList = new JList(model);

        projectList.setCellRenderer(new ListItemCellRenderer());
        add(projectList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("addRole")));
        toolbar.add(new JButton(am.get("removeRole")));
        add(toolbar, BorderLayout.SOUTH);
    }

    public JList getProjectList()
    {
        return projectList;
    }
}