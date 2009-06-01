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
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.GroupsOrganizationSearch;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableMultipleSelectionModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.UsersOrganizationSearch;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

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
    UnitOfWorkFactory uowf;

    @Service
    ProjectModel projectModel;

    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addUsersView;
    private TableSelectionView addGroupsView;

    public AddMemberDialog(@Service ApplicationContext context,
                           @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        TableMultipleSelectionModel usersModel = obf.newObject(TableMultipleSelectionModel.class);
        this.addUsersView = obf.newObjectBuilder(TableSelectionView.class).use(usersModel, "#Search users").newInstance();

        TableMultipleSelectionModel groupsModel = obf.newObject(TableMultipleSelectionModel.class);
        this.addGroupsView= obf.newObjectBuilder(TableSelectionView.class).use(groupsModel, "#Search groups").newInstance();

        JSplitPane dialog = new JSplitPane();

        final UsersOrganizationSearch usersSearch = obf.newObjectBuilder(UsersOrganizationSearch.class).use(addUsersView).newInstance();
        addUsersView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                usersSearch.search();
            }
        });

        final GroupsOrganizationSearch groupsSearch = obf.newObjectBuilder(GroupsOrganizationSearch.class).use(addGroupsView).newInstance();
        addGroupsView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                groupsSearch.search();
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
        Set<ListItemValue> selected = ((TableMultipleSelectionModel)addUsersView.getModel()).getSelected();
        selected.addAll(((TableMultipleSelectionModel)addGroupsView.getModel()).getSelected());

        projectModel.addMembers(selected);
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}