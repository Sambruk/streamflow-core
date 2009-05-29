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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddProjectsView;
import se.streamsource.streamflow.client.ui.administration.projects.members.AddUsersView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * JAVADOC
 */
public class ForwardSharedTasksDialog
        extends JPanel
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    ProjectModel projectModel;

    @Service
    IndividualRepository individual;

    @Service
    SharedInboxView sharedInboxView;

    @Service
    SharedInboxModel sharedInboxModel;


    @Service
    Restlet client;


    Dimension dialogSize = new Dimension(600,300);
    private AddUsersView addUsersview;
    private AddProjectsView addProjectsView;

    public ForwardSharedTasksDialog(@Service ApplicationContext context,
                                    @Uses final AddUsersView addUsersView,
                                    @Uses final AddProjectsView addProjectsView)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));
        this.addUsersview = addUsersView;
        this.addProjectsView = addProjectsView;

        JSplitPane dialog = new JSplitPane();
        addUsersView.setKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                // Search for users!
                final List<ListValue> results = new ArrayList<ListValue>();
                individual.individual().visitAccounts(new AccountVisitor(){

                    public void visitAccount(Account account)
                    {

                        try
                        {
                            results.add(account.user(client).findUsers(addUsersView.searchText()));
                        } catch (ResourceException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                System.out.println("User accounts: " +results.size());
                if (results.size() > 0)
                {
                    addUsersview.getModel().setModel(results.get(0));
                }
            }
        });

        addProjectsView.setKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                final List<ListValue> results = new ArrayList<ListValue>();
                individual.individual().visitAccounts(new AccountVisitor(){

                    public void visitAccount(Account account)
                    {
                        try
                        {
                            results.add(account.user(client).findProjects(addProjectsView.searchText()));
                        } catch (ResourceException e)
                        {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                });
                if (results.size() >0)
                {
                    addProjectsView.getModel().setModel(results.get(0));
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
        Set<ListItemValue> selected = addUsersview.getModel().getSelected();
        selected.addAll(addProjectsView.getModel().getSelected());
        if (!selected.isEmpty())
        {
            // hack hack hack... get one element
            ListItemValue theSelected = null;
            for (ListItemValue value : selected)
            {
                theSelected = value;
                break;
            }
            try
            {
                Iterable<InboxTaskValue> selectedTasks = sharedInboxView.getSelectedTasks();
                for (InboxTaskValue selectedTask : selectedTasks)
                {
                    sharedInboxModel.forward(selectedTask.task().get().identity(), theSelected.entity().get().identity());
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