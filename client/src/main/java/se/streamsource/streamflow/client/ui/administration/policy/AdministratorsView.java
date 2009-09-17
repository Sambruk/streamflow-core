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

package se.streamsource.streamflow.client.ui.administration.policy;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.administration.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Set;

/**
 * JAVADOC
 */
public class AdministratorsView
        extends JPanel
{
    AdministratorsModel model;

    @Service
    DialogService dialogs;

    @Uses
    Iterable<SelectUsersAndGroupsDialog> selectUsersAndGroupsDialogs;

    public JList administratorList;

    public AdministratorsView(@Service ApplicationContext context, @Uses final AdministratorsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(context.getActionMap(this));

        administratorList = new JList(model);

        administratorList.setCellRenderer(new ListItemCellRenderer());
        add(administratorList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        add(toolbar, BorderLayout.SOUTH);

        addAncestorListener(new RefreshWhenVisible(model));
    }

    @Action
    public void add()
    {
        SelectUsersAndGroupsDialog dialog = selectUsersAndGroupsDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        Set<String> added = dialog.getUsersAndGroups();
        if (added != null)
        {
            for (String identity : added)
            {
                model.addAdministrator(identity);
            }
        }
    }

    @Action
    public void remove()
    {
        ListItemValue selected = (ListItemValue) administratorList.getSelectedValue();
        model.removeAdministrator(selected.entity().get().identity());
    }
}