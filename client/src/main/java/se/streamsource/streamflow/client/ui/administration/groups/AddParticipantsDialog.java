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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.HashSet;

import se.streamsource.streamflow.client.ui.administration.projects.members.AddGroupsView;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddUsersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

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


    Dimension dialogSize = new Dimension(600,300);
    private AddGroupsView addGroupsView;
    private AddUsersView addUsersview;

    @Service
    GroupsView groupsView;

    public AddParticipantsDialog(@Service ApplicationContext context,
                           @Uses AddUsersView addUsersView,
                           @Uses AddGroupsView addGroupsView)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));
        this.addGroupsView = addGroupsView;
        this.addUsersview = addUsersView;

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
        Set<ListItemValue> users = addUsersview.getModel().getSelected().keySet();
        Set<ListItemValue> groups = addGroupsView.getModel().getSelected().keySet();

        Set<ListItemValue> selected = new HashSet<ListItemValue>(users.size() + groups.size());
        selected.addAll(users);
        selected.addAll(groups);

        // remove the current group, it cannot be added to itself
        ListItemValue selectedItem = (ListItemValue) groupsView.getGroupList().getSelectedValue();
        selected.remove(selectedItem);
        WindowUtils.findJDialog(this).dispose();
        groupModel.addParticipants(selected);
    }

    @Action
    public void close()
    {
        WindowUtils.findJDialog(this).dispose();
    }
}