/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.AbstractTableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddGroupsView;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddUsersView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * JAVADOC
 */
public class AddParticipantsDialog
        extends JPanel
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    GroupModel groupModel;

    @Service
    OrganizationalUnitAdministrationModel organizationModel;

    Dimension dialogSize = new Dimension(600,300);
    private AbstractTableSelectionView addGroupsView;
    private AbstractTableSelectionView addUsersview;

    @Service
    GroupsView groupsView;

    public AddParticipantsDialog(@Service ApplicationContext context,
                           @Uses final AddUsersView addUsersView,
                           @Uses final AddGroupsView addGroupsView)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));
        this.addGroupsView = addGroupsView;
        this.addUsersview = addUsersView;

        addUsersView.setKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue list = organizationModel.getOrganization().findUsers(addUsersView.searchText());
                    addUsersView.getModel().setModel(list);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
        addGroupsView.setKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue list = organizationModel.getOrganization().findGroups(addGroupsView.searchText());
                    addGroupsView.getModel().setModel(list);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });

        JSplitPane dialog = new JSplitPane();

        dialog.setLeftComponent(addUsersView);
        dialog.setRightComponent(addGroupsView);
        dialog.setPreferredSize(dialogSize);
        setPreferredSize(dialogSize);
        add(dialog, BorderLayout.NORTH);
    }

    @Action
    public void execute()
    {
        Set<ListItemValue> selected = addUsersview.getModel().getSelected();
        selected.addAll(addGroupsView.getModel().getSelected());

        // remove the current group, it cannot be added to itself
        ListItemValue selectedItem = (ListItemValue) groupsView.getGroupList().getSelectedValue();
        selected.remove(selectedItem);
        WindowUtils.findWindow(this).dispose();
        groupModel.addParticipants(selected);
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}