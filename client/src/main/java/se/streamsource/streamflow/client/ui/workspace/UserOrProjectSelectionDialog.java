/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSingleSelectionModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class UserOrProjectSelectionDialog
        extends JPanel
{
    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addUsersView;
    private TableSelectionView addProjectsView;
    public ListItemValue selected;

    public UserOrProjectSelectionDialog(final @Uses UserNode user,
                               @Service ApplicationContext context,
                               @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        setName("#Search user or project");
        setActionMap(context.getActionMap(this));

        TableSingleSelectionModel usersModel = obf.newObject(TableSingleSelectionModel.class);
        this.addUsersView = obf.newObjectBuilder(TableSelectionView.class).use(usersModel, "#Search users").newInstance();

        TableSingleSelectionModel projectsModel = obf.newObject(TableSingleSelectionModel.class);
        this.addProjectsView = obf.newObjectBuilder(TableSelectionView.class).use(projectsModel, "#Search projects").newInstance();

        JSplitPane dialog = new JSplitPane();

        addUsersView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue value = user.findUsers(addUsersView.searchText());
                    addUsersView.getModel().setModel(value);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });

        addProjectsView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue value = user.findProjects(addProjectsView.searchText());
                    addProjectsView.getModel().setModel(value);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
        addProjectsView.getModel().addTableModelListener(new TableModelListener(){

            public void tableChanged(TableModelEvent tableModelEvent)
            {
                if (TableModelEvent.UPDATE == tableModelEvent.getType())
                {
                    addUsersView.getModel().clearSelection();
                }
            }
        });

        addUsersView.getModel().addTableModelListener(new TableModelListener(){

            public void tableChanged(TableModelEvent tableModelEvent)
            {
                if (TableModelEvent.UPDATE == tableModelEvent.getType())
                {
                    addProjectsView.getModel().clearSelection();
                }
            }
        });

        
        dialog.setLeftComponent(addUsersView);
        dialog.setRightComponent(addProjectsView);
        dialog.setPreferredSize(dialogSize);
        setPreferredSize(dialogSize);
        add(dialog, BorderLayout.NORTH);
    }

    public EntityReference getSelected()
    {
        return selected == null ? null : selected.entity().get();
    }

    @Action
    public void execute()
    {
        selected = ((TableSingleSelectionModel) addUsersView.getModel()).getSelected();
        if (selected == null)
        {
            selected = ((TableSingleSelectionModel) addProjectsView.getModel()).getSelected();
        }

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}