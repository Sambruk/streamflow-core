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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSingleSelectionModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class DelegateTasksDialog
        extends JPanel
{

    @Service
    ProjectModel projectModel;

    @Service
    UserInboxView userInboxView;

    @Service
    UserInboxModel userInboxModel;

    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addUsersView;
    private TableSelectionView addProjectsView;

    public DelegateTasksDialog(@Service ApplicationContext context,
                                     @Structure ObjectBuilderFactory obf,
                                     @Structure UnitOfWorkFactory uowf)
    {
        super(new BorderLayout());

        uowf.newUnitOfWork();
        setName("#Search user or project to delegate to");
        setActionMap(context.getActionMap(this));

        TableSingleSelectionModel usersModel = obf.newObject(TableSingleSelectionModel.class);
        this.addUsersView = obf.newObjectBuilder(TableSelectionView.class).use(usersModel, "#Search users").newInstance();

        TableSingleSelectionModel projectsModel = obf.newObject(TableSingleSelectionModel.class);
        this.addProjectsView = obf.newObjectBuilder(TableSelectionView.class).use(projectsModel, "#Search projects").newInstance();



        JSplitPane dialog = new JSplitPane();

        final UsersIndividualSearch usersSearch = obf.newObjectBuilder(UsersIndividualSearch.class).use(addUsersView).newInstance();
        addUsersView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                usersSearch.search();
            }
        });

        final ProjectsIndividualSearch projectsSearch = obf.newObjectBuilder(ProjectsIndividualSearch.class).use(addProjectsView).newInstance();
        addProjectsView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                projectsSearch.search();
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

    @Action
    public void execute()
    {
        ListItemValue selected = ((TableSingleSelectionModel) addUsersView.getModel()).getSelected();
        if (selected == null)
        {
            selected = ((TableSingleSelectionModel) addProjectsView.getModel()).getSelected();
        }

        if (selected != null)
        {
            try
            {
                Iterable<InboxTaskDTO> selectedTasks = userInboxView.getSelectedTasks();
                for (InboxTaskDTO selectedTask : selectedTasks)
                {
                    userInboxModel.delegate(selectedTask.task().get().identity(), selected.entity().get().identity());
                }
            } catch(ResourceException e)
            {
                e.printStackTrace();
            }
        }

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}