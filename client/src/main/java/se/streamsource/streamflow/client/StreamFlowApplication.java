/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client;

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.error.ErrorInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.administration.groups.AddParticipantsDialog;
import se.streamsource.streamflow.client.ui.administration.groups.GroupModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.groups.NewGroupDialog;
import se.streamsource.streamflow.client.ui.administration.projects.AddMemberDialog;
import se.streamsource.streamflow.client.ui.administration.projects.NewProjectDialog;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.roles.NewRoleDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;
import se.streamsource.streamflow.client.ui.menu.CreateAccountDialog;
import se.streamsource.streamflow.client.ui.navigator.NavigatorView;
import se.streamsource.streamflow.client.ui.status.StatusBarView;
import se.streamsource.streamflow.client.ui.status.StatusResources;
import se.streamsource.streamflow.client.ui.shared.AddCommentDialog;
import se.streamsource.streamflow.client.ui.shared.AddTaskDialog;
import se.streamsource.streamflow.client.ui.shared.DelegateTasksDialog;
import se.streamsource.streamflow.client.ui.shared.ForwardTasksDialog;
import se.streamsource.streamflow.client.ui.shared.TaskCommentsModel;
import se.streamsource.streamflow.client.ui.shared.UserAssignmentsModel;
import se.streamsource.streamflow.client.ui.shared.UserAssignmentsView;
import se.streamsource.streamflow.client.ui.shared.UserDelegationsModel;
import se.streamsource.streamflow.client.ui.shared.UserDelegationsView;
import se.streamsource.streamflow.client.ui.shared.UserInboxModel;
import se.streamsource.streamflow.client.ui.shared.UserInboxView;
import se.streamsource.streamflow.client.ui.shared.UserWaitingForModel;
import se.streamsource.streamflow.client.ui.shared.UserWaitingForView;
import se.streamsource.streamflow.client.ui.shared.WorkspaceModel;
import se.streamsource.streamflow.client.ui.shared.WorkspaceView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.roles.DescriptionDTO;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the application
 */
public class StreamFlowApplication
        extends SingleFrameApplication
{
    public
    @Service
    NavigatorView navigatorView;

    public StreamFlowApplication()
    {
        super();

        getContext().getResourceManager().setApplicationBundleNames(Arrays.asList("se.streamsource.streamflow.client.resources.StreamFlowApplication"));

        JXFrame frame = new JXFrame();

        frame.setLocationByPlatform(true);

        JXStatusBar bar = new StatusBarView(getContext());
        frame.setStatusBar(bar);

        setMainFrame(frame);
    }

    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    DialogService dialogs;

    @Service
    IndividualRepository individualRepo;

    JLabel label;

    public void init(@Structure ObjectBuilderFactory obf) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
    {
        JFrame frame = getMainFrame();
        frame.setTitle("StreamFlow");

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(navigatorView, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
        frame.setVisible(true);

        frame.setJMenuBar(navigatorView.getMenu());

        frame.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                super.keyTyped(e);
            }
        });

    }

    @Override
    protected void startup()
    {
        try
        {
            Energy4Java is = new Energy4Java();
            ApplicationSPI app = is.newApplication(new StreamFlowClientAssembler());
            app.metaInfo().set(this);
            app.metaInfo().set(org.jdesktop.application.Application.getInstance().getContext());
            app.metaInfo().set(getContext().getActionMap(this));

            Client client = new Client(Protocol.HTTP);
            client.start();
            // Make it slower to get it more realistic
            Restlet restlet = new Filter(client.getContext(), client)
            {
                @Override
                protected int beforeHandle(Request request, Response response)
                {
                    Logger.getLogger(LoggerCategories.STATUS).info(StatusResources.loading.name());
                    Logger.getLogger(LoggerCategories.PROGRESS).info("loading");
/*
                    try
                    {
                        Thread.sleep(2000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
*/
                    return super.beforeHandle(request, response);
                }

                @Override
                protected void afterHandle(Request request, Response response)
                {
                    Logger.getLogger(LoggerCategories.STATUS).info(StatusResources.ready.name());
                    Logger.getLogger(LoggerCategories.PROGRESS).info(LoggerCategories.DONE);

                    super.afterHandle(request, response);
                }
            };
            app.metaInfo().set(restlet);

            Logger.getLogger(getClass().getName()).info("Starting in " + app.mode() + " mode");

            app.activate();
        } catch (Throwable e)
        {
            JXErrorPane.showDialog(getMainFrame(), new ErrorInfo("#startuperror", e.getMessage(), null, "#error", e, Level.SEVERE, Collections.<String, String>emptyMap()));
            System.out.println(e);
        }
        System.out.println("Startup done");

    }

    // Menu actions

    @Uses
    private ObjectBuilder<AccountsDialog> accountsDialog;

    @Uses
    private ObjectBuilder<CreateAccountDialog> createAccountDialog;


    @Service
    AccountsModel accountsModel;

    @Service
    WorkspaceModel workspaceModel;

    @Action
    public void manageAccounts()
    {
        AccountsDialog dialog = accountsDialog.newInstance();
        accountsModel.refresh();
        dialogs.showOkCancelHelpDialog(getMainFrame(), dialog);
    }

    @Action
    public void deleteAccount()
    {
        //accountsModel.getSelected();
    }

    @Action
    public void createAccount()
    {
        CreateAccountDialog dialog = createAccountDialog.newInstance();
        dialogs.showOkCancelHelpDialog(getMainFrame(), dialog);
        accountsModel.refresh();
        workspaceView.refreshTree();
        administrationModel.refresh();
    }

    // Controller actions -------------------------------------------
    @Service
    WorkspaceView workspaceView;

    // Menu actions
    // Account menu
    @Action
    public void showAdministration()
    {
        uowf.newUnitOfWork(newUsecase("Administration"));
        dialogs.showOkDialog(getMainFrame(), navigatorView.getAdministration());
    }

    @Action
    public void ok()
    {
        try
        {
            uowf.currentUnitOfWork().complete();
        } catch (UnitOfWorkCompletionException e)
        {
            dialogs.showOkDialog(this.getMainFrame(), new JLabel("#couldnotcomplete:" + e.getMessage()));
        }
    }

    @Action
    public void cancel()
    {
        SwingUtilities.windowForComponent(getContext().getFocusOwner()).setVisible(false);
        uowf.currentUnitOfWork().discard();
    }

    @Action
    public void help()
    {
        JOptionPane.showMessageDialog(this.getMainFrame(), "#showhelp");
    }

    // User inbox actions ------------------------------------
    @Uses
    private ObjectBuilder<AddTaskDialog> addSharedTaskDialogs;
    @Uses
    private ObjectBuilder<ForwardTasksDialog> forwardSharedTasksDialog;

    @Service
    UserInboxView userInboxView;

    @Service
    UserInboxModel userInboxModel;

    @Service
    TaskCommentsModel taskCommentsModel;

    @Action()
    public void newUserInboxTask()
    {
        // Show dialog
        AddTaskDialog dialog = addSharedTaskDialogs.newInstance();
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), dialog);

        NewSharedTaskCommand command = dialog.getCommandBuilder().newInstance();
        try
        {
            userInboxModel.newTask(command);

            JXTreeTable table = userInboxView.getTaskTable();
            int index = userInboxModel.getChildCount(userInboxModel.getRoot());
            Object child = userInboxModel.getChild(userInboxModel, index - 1);
            TreePath path = new TreePath(child);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().addSelectionInterval(index-1, index-1);
            table.scrollPathToVisible(path);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    @Action()
    public void addUserInboxSubTask()
    {
        // Show dialog
        AddTaskDialog dialog = addSharedTaskDialogs.newInstance();
        InboxTaskDTO selected = userInboxView.getSelectedTask();
        dialog.getCommandBuilder().prototype().parentTask().set(selected.task().get());
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), dialog);
    }

    @Action
    public void assignTasksToMe() throws ResourceException
    {
        int selection = userInboxView.getTaskTable().getSelectedRow();
        Iterable<InboxTaskDTO> selectedTasks = userInboxView.getSelectedTasks();
        for (InboxTaskDTO selectedTask : selectedTasks)
        {
            userInboxModel.assignToMe(selectedTask.task().get().identity());
        }
        userInboxView.getTaskTable().getSelectionModel().setSelectionInterval(selection, selection);
        userInboxView.repaint();
    }

    @Action
    public void delegateTasksFromInbox() throws ResourceException
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(DelegateTasksDialog.class).newInstance());
    }

    @Action
    public Task refreshUserInbox() throws ResourceException
    {
        return new Task(this)
        {
            protected Object doInBackground() throws Exception
            {
                userInboxModel.refresh();
                return null;
            }
        };
    }

    @Action
    public void removeUserInboxTasks() throws ResourceException
    {
        Iterable<InboxTaskDTO> selected = userInboxView.getSelectedTasks();
        for (InboxTaskDTO taskValue : selected)
        {
            userInboxModel.removeTask(taskValue.task().get().identity());
        }
    }

    @Action
    public void forwardTasksTo()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(ForwardTasksDialog.class).newInstance());
    }

    @Action
    public void addTaskComment() throws ResourceException, IOException
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(AddCommentDialog.class).newInstance());
    }

    // Shared user assignments actions ------------------------------
    @Service
    UserAssignmentsView userAssignmentsView;
    @Service
    UserAssignmentsModel userAssignmentsModel;

    @Action
    public void refreshSharedAssignments() throws ResourceException
    {
        userAssignmentsModel.refresh();
    }

    @Action
    public void removeSharedAssignedTasks() throws ResourceException
    {
        Iterable<AssignedTaskDTO> selected = userAssignmentsView.getSelectedTasks();
        for (AssignedTaskDTO taskDTO : selected)
        {
            userAssignmentsModel.removeTask(taskDTO.task().get().identity());
        }
    }

    // Shared user delegations actions ------------------------------
    @Service
    UserDelegationsView userDelegationsView;
    @Service
    UserDelegationsModel userDelegationsModel;

    @Action
    public void refreshSharedDelegations() throws ResourceException
    {
        userDelegationsModel.refresh();
    }

    @Action
    public void assignDelegatedTasksToMe() throws ResourceException
    {
        Iterable<DelegatedTaskDTO> task = userDelegationsView.getSelectedTasks();
        for (DelegatedTaskDTO delegatedTaskValue : task)
        {
            userDelegationsModel.assignToMe(delegatedTaskValue.task().get().identity());
        }
    }

    @Action
    public void rejectUserDelegations() throws ResourceException
    {
        Iterable<DelegatedTaskDTO> task = userDelegationsView.getSelectedTasks();
        for (DelegatedTaskDTO delegatedTaskValue : task)
        {
            userDelegationsModel.reject(delegatedTaskValue.task().get().identity());
        }
    }

    // Shared user waiting for actions ------------------------------
    @Service
    UserWaitingForView userWaitingForView;
    @Service
    UserWaitingForModel userWaitingForModel;

    @Action
    public void delegateWaitingForTask()
    {
    }

    @Action
    public void refreshSharedWaitingFor() throws ResourceException
    {
        userWaitingForModel.refresh();
    }

    // Group administration actions ---------------------------------
    @Service
    AdministrationModel administrationModel;

    @Service
    GroupsView groupsView;

    @Service
    GroupsModel groupsModel;

    @Service
    GroupView groupView;
    @Service
    GroupModel groupModel;

    @Action
    public void addGroup()
    {
        ValueBuilder<DescriptionDTO> newGroupBuilder = vbf.newValueBuilder(DescriptionDTO.class);
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(NewGroupDialog.class).use(newGroupBuilder).newInstance());
    }

    @Action
    public void removeGroup()
    {
        ListItemValue selected = (ListItemValue) groupsView.getGroupList().getSelectedValue();
        groupsModel.removeGroup(selected.entity().get().identity());
    }

    @Action
    public void addParticipant()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(AddParticipantsDialog.class).newInstance());
    }

    @Action
    public void removeParticipant()
    {
        ListItemValue value = (ListItemValue) groupView.getParticipantList().getSelectedValue();
        groupModel.removeParticipant(value.entity().get());
    }

    // Project administration actions -------------------------------

    @Service
    ProjectsModel projectsModel;
    @Service
    ProjectsView projectsView;
    @Service
    ProjectModel projectModel;
    @Service
    ProjectView projectView;

    @Action
    public void addProject()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(NewProjectDialog.class).newInstance());
    }

    @Action
    public void removeProject()
    {
        ListItemValue selected = (ListItemValue) projectsView.getProjectList().getSelectedValue();
        projectsModel.removeProject(selected.entity().get().identity());
    }

    @Action
    public void addMember()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObject(AddMemberDialog.class));
    }

    @Action
    public void removeMember()
    {
        if (projectView.getMembers().getSelectionPath() != null)
        {
            TreeNodeValue selected = (TreeNodeValue) projectView.getMembers().getSelectionPath().getPathComponent(1);
            projectModel.removeMember(selected.entity().get());
        }
        workspaceView.refreshTree();
    }

    // Role administration actions ----------------------------------

    @Action
    public void addRole()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObject(NewRoleDialog.class));
    }

    @Action
    public void removeRole()
    {
        // TODO
    }

}