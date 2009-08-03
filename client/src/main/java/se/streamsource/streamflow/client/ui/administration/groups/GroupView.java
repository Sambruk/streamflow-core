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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.ui.administration.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Set;

/**
 * JAVADOC
 */
public class GroupView
        extends JPanel
{
    @Service
    DialogService dialogs;

    @Uses
    Iterable<SelectUsersAndGroupsDialog> selectUsersAndGroups;

    public JList participantList;

    private GroupModel model;

    public GroupView(@Service ActionMap am, @Uses GroupModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(am);

        participantList = new JList(model);

        participantList.setCellRenderer(new ListItemCellRenderer());

        add(participantList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
    }

    @Action
    public void add()
    {
        SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        Set<String> participants = dialog.getUsersAndGroups();
        if (participants != null)
        {
            model.addParticipants(participants);
        }
    }

    @Action
    public void remove()
    {
        ListItemValue value = (ListItemValue) participantList.getSelectedValue();
        model.removeParticipant(value.entity().get().identity());
    }

    public JList getParticipantList()
    {
        return participantList;
    }
}