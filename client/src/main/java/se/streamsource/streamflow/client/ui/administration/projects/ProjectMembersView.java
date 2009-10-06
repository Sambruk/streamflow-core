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
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersView
        extends JPanel
{
    @Service
    DialogService dialogs;

    @Uses
    Iterable<SelectUsersAndGroupsDialog> selectUsersAndGroups;

    @Structure
    ObjectBuilderFactory obf;

    public JXList membersList;
    private ProjectMembersModel membersModel;

    public ProjectMembersView(@Service ApplicationContext context,
                              @Uses final ProjectMembersModel membersModel)
    {
        super(new BorderLayout());
        this.membersModel = membersModel;

        setActionMap(context.getActionMap(this));

        membersList = new JXList(membersModel);

        membersList.setCellRenderer( new ListItemCellRenderer() );

        add( membersList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        membersList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler(getActionMap().get("remove")));
    }


    @Action
    public void add()
    {
        SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        Set<String> members = dialog.getUsersAndGroups();
        if (members != null)
        {
            membersModel.addMembers(members);
            membersModel.refresh();
        }
    }

    @Action
    public void remove()
    {
        if (!membersList.isSelectionEmpty())
        {
            membersModel.removeMember(membersList.getSelectedIndex());
            membersModel.refresh();
        }
    }
}