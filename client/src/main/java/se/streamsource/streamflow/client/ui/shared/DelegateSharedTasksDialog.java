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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddProjectsView;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddUsersView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * JAVADOC
 */
public class DelegateSharedTasksDialog
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
    private AddUsersView addUsersview;
    private AddProjectsView addProjectsView;

    public DelegateSharedTasksDialog(@Service ApplicationContext context,
                                    @Uses final AddUsersView addUsersView,
                                    @Uses final AddProjectsView addProjectsView,
                                    @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        setName("#Search user or project to delegate to");
        setActionMap(context.getActionMap(this));
        this.addUsersview = addUsersView;
        this.addProjectsView = addProjectsView;

        JSplitPane dialog = new JSplitPane();

        final UsersIndividualSearch usersSearch = obf.newObjectBuilder(UsersIndividualSearch.class).use(addUsersView).newInstance();
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
        
        dialog.setLeftComponent(addUsersView);
        dialog.setRightComponent(addProjectsView);
        dialog.setPreferredSize(dialogSize);
        setPreferredSize(dialogSize);
        add(dialog, BorderLayout.NORTH);
    }

    @Action
    public void execute()
    {
        Set<ListItemValue> selected = addUsersview.getModel().getSelected();
        selected.addAll(addProjectsView.getModel().getSelected());
        if (selected.size() == 1)
        {
            ListItemValue theSelected = (ListItemValue) selected.toArray()[0];
            try
            {
                Iterable<InboxTaskDTO> selectedTasks = sharedInboxView.getSelectedTasks();
                for (InboxTaskDTO selectedTask : selectedTasks)
                {
                    sharedInboxModel.delegate(selectedTask.task().get().identity(), theSelected.entity().get().identity());
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