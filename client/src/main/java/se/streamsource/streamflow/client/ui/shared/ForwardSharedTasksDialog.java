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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class ForwardSharedTasksDialog
        extends JPanel
{

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    ProjectModel projectModel;

    @Service
    SharedInboxView sharedInboxView;

    @Service
    SharedInboxModel sharedInboxModel;

    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addUsersview;
    private TableSelectionView addProjectsView;

    public ForwardSharedTasksDialog(@Service ApplicationContext context,
                                    @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        this.setName("#Search user or project to send to");
        setActionMap(context.getActionMap(this));

        TableSingleSelectionModel usersModel = obf.newObject(TableSingleSelectionModel.class);
        this.addUsersview = obf.newObjectBuilder(TableSelectionView.class).use(usersModel, "#Search users").newInstance();

        TableSingleSelectionModel projectsModel = obf.newObject(TableSingleSelectionModel.class);
        this.addProjectsView = obf.newObjectBuilder(TableSelectionView.class).use(projectsModel, "#Search projects").newInstance();

        JSplitPane dialog = new JSplitPane();


        final UsersIndividualSearch usersSearch = obf.newObjectBuilder(UsersIndividualSearch.class).use(addUsersview).newInstance();
        addUsersview.getSearchInputField().addKeyListener(new KeyAdapter(){
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
                    addUsersview.getModel().clearSelection();
                }
            }
        });
        
        addUsersview.getModel().addTableModelListener(new TableModelListener(){

            public void tableChanged(TableModelEvent tableModelEvent)
            {
                if (TableModelEvent.UPDATE == tableModelEvent.getType()) 
                {
                    addProjectsView.getModel().clearSelection();
                }
            }
        });

        dialog.setLeftComponent(addUsersview);
        dialog.setRightComponent(addProjectsView);
        dialog.setPreferredSize(dialogSize);
        setPreferredSize(dialogSize);
        add(dialog, BorderLayout.NORTH);
    }

    @Action
    public void execute()
    {
        ListItemValue selected = ((TableSingleSelectionModel) addUsersview.getModel()).getSelected();
        if (selected == null)
        {
            selected = ((TableSingleSelectionModel) addProjectsView.getModel()).getSelected();
        }

        if (selected != null)
        {
            try
            {
                Iterable<InboxTaskDTO> selectedTasks = sharedInboxView.getSelectedTasks();
                for (InboxTaskDTO selectedTask : selectedTasks)
                {
                    sharedInboxModel.forward(selectedTask.task().get().identity(), selected.entity().get().identity());
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