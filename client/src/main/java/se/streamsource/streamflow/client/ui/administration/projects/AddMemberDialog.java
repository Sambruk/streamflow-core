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

package se.streamsource.streamflow.client.ui.administration.projects;

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
public class AddMemberDialog
        extends JPanel
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    ProjectModel projectModel;

    @Service
    OrganizationalUnitAdministrationModel organizationModel;


    Dimension dialogSize = new Dimension(600,300);
    private AddUsersView addUsersview;
    private AddGroupsView addGroupsView;

    public AddMemberDialog(@Service ApplicationContext context,
                           @Uses final AddUsersView addUsersView,
                           @Uses final AddGroupsView addGroupsView)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));
        this.addUsersview = addUsersView;
        this.addGroupsView = addGroupsView;

        JSplitPane dialog = new JSplitPane();

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

        projectModel.addMembers(selected);
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}