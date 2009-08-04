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

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableMultipleSelectionModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * JAVADOC
 */
public class SelectUsersAndGroupsDialog
        extends JPanel
{
    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addGroupsView;
    private TableSelectionView addUsersView;

    private Set<String> usersAndGroups;

    public SelectUsersAndGroupsDialog(@Service ApplicationContext context,
                                 final @Uses OrganizationalUnitAdministrationModel organizationModel,
                                 @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        TableMultipleSelectionModel usersModel = obf.newObject(TableMultipleSelectionModel.class);
        this.addUsersView = obf.newObjectBuilder(TableSelectionView.class).use(usersModel, "#Search users").newInstance();

        TableMultipleSelectionModel groupsModel = obf.newObject(TableMultipleSelectionModel.class);
        this.addGroupsView= obf.newObjectBuilder(TableSelectionView.class).use(groupsModel, "#Search groups").newInstance();

        addUsersView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
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

        addGroupsView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
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
        usersAndGroups = ((TableMultipleSelectionModel)addUsersView.getModel()).getSelected();
        usersAndGroups.addAll(((TableMultipleSelectionModel)addGroupsView.getModel()).getSelected());

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

    public Set<String> getUsersAndGroups()
    {
        return usersAndGroups;
    }
}