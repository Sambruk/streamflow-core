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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class GroupsView
        extends JPanel
{
    GroupsModel model;

    @Structure
    ObjectBuilderFactory obf;

    @Service
    DialogService dialogs;

    public JList groupList;

    public GroupsView(@Service ApplicationContext context, @Uses final GroupsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        groupList = new JList(model);

        groupList.setCellRenderer(new ListItemCellRenderer());

        add(groupList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);

        groupList.addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
    }

    @Action
    public void add()
    {
        NameDialog dialog = obf.newObject(NameDialog.class);
        dialogs.showOkCancelHelpDialog(this, dialog);
        String name = dialog.name();
        if (name != null)
        {
            model.newGroup(name);
        }
    }

    @Action
    public void remove()
    {
        ListItemValue selected = (ListItemValue) groupList.getSelectedValue();
        model.removeGroup(selected.entity().get().identity());
    }

    public JList getGroupList()
    {
        return groupList;
    }

}
