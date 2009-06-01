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
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
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
import org.restlet.data.Protocol;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
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
import se.streamsource.streamflow.client.ui.navigator.NavigatorView;
import se.streamsource.streamflow.client.ui.shared.AddCommentDialog;
import se.streamsource.streamflow.client.ui.shared.AddSharedTaskDialog;
import se.streamsource.streamflow.client.ui.shared.DelegateSharedTasksDialog;
import se.streamsource.streamflow.client.ui.shared.ForwardSharedTasksDialog;
import se.streamsource.streamflow.client.ui.shared.SharedAssignmentsModel;
import se.streamsource.streamflow.client.ui.shared.SharedAssignmentsView;
import se.streamsource.streamflow.client.ui.shared.SharedDelegationsModel;
import se.streamsource.streamflow.client.ui.shared.SharedDelegationsView;
import se.streamsource.streamflow.client.ui.shared.SharedInboxModel;
import se.streamsource.streamflow.client.ui.shared.SharedInboxView;
import se.streamsource.streamflow.client.ui.shared.SharedView;
import se.streamsource.streamflow.client.ui.shared.TaskCommentsModel;
import se.streamsource.streamflow.client.ui.status.StatusBarView;
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
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
            app.metaInfo().set(client);

            Logger.getLogger(getClass().getName()).info("Starting in " + app.mode() + " mode");

            app.activate();
        } catch (Throwable e)
        {
            JXErrorPane.showDialog(getMainFrame(), new ErrorInfo("#startuperror", e.getMessage(), null, "#error", e, Level.SEVERE, Collections.<String, String>emptyMap()));
            System.out.println(e);
        }
        System.out.println("Startup done");

    }

    // Controller actions -------------------------------------------
    @Service
    SharedView sharedView;

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

    // Shared users inbox actions ------------------------------------
    @Uses
    private ObjectBuilder<AddSharedTaskDialog> addSharedTaskDialogs;
    @Uses
    private ObjectBuilder<ForwardSharedTasksDialog> forwardSharedTasksDialog;

    @Service
    SharedInboxView sharedInboxView;

    @Service
    SharedInboxModel sharedInboxModel;

    @Service
    TaskCommentsModel taskCommentsModel;

    @Action()
    public void newSharedTask()
    {
        // Show dialog
        AddSharedTaskDialog dialog = addSharedTaskDialogs.newInstance();
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), dialog);
    }

    @Action()
    public void addSharedSubTask()
    {
        // Show dialog
        AddSharedTaskDialog dialog = addSharedTaskDialogs.newInstance();
        InboxTaskDTO selected = sharedInboxView.getSelectedTask();
        dialog.getCommandBuilder().prototype().parentTask().set(selected.task().get());
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), dialog);
    }

    @Action
    public void assignTasksToMe() throws ResourceException
    {
        Iterable<InboxTaskDTO> selectedTasks = sharedInboxView.getSelectedTasks();
        for (InboxTaskDTO selectedTask : selectedTasks)
        {
            sharedInboxModel.assignToMe(selectedTask.task().get().identity());
        }
    }

    @Action
    public void delegateTasksFromInbox() throws ResourceException
    {
        uowf.nestedUnitOfWork();
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(DelegateSharedTasksDialog.class).newInstance());
    }

    @Action
    public void refreshSharedInbox() throws ResourceException
    {
        sharedInboxModel.refresh();
    }

    @Action
    public void removeSharedTasks() throws ResourceException
    {
        Iterable<InboxTaskDTO> selected = sharedInboxView.getSelectedTasks();
        for (InboxTaskDTO taskValue : selected)
        {
            sharedInboxModel.removeTask(taskValue.task().get().identity());
        }
    }

    @Action
    public void forwardSharedTasksTo()
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(ForwardSharedTasksDialog.class).newInstance());
    }

    @Action
    public void addTaskComment() throws ResourceException, IOException
    {
        dialogs.showOkCancelHelpDialog(this.getMainFrame(), obf.newObjectBuilder(AddCommentDialog.class).newInstance());
    }

    // Shared user assignments actions ------------------------------
    @Service SharedAssignmentsView sharedAssignmentsView;
    @Service SharedAssignmentsModel sharedAssignmentsModel;

    @Action
    public void refreshSharedAssignments() throws ResourceException
    {
        sharedAssignmentsModel.refresh();
    }

    @Action
    public void removeSharedAssignedTasks() throws ResourceException
    {
        Iterable<AssignedTaskDTO> selected = sharedAssignmentsView.getSelectedTasks();
        for (AssignedTaskDTO taskDTO : selected)
        {
            sharedAssignmentsModel.removeTask(taskDTO.task().get().identity());
        }
    }

    // Shared user delegations actions ------------------------------
    @Service SharedDelegationsView sharedDelegationsView;
    @Service SharedDelegationsModel sharedDelegationsModel;

    @Action
    public void refreshSharedDelegations() throws ResourceException
    {
        sharedDelegationsModel.refresh();
    }

    @Action
    public void assignDelegatedTasksToMe() throws ResourceException
    {
        Iterable<DelegatedTaskDTO> task = sharedDelegationsView.getSelectedTasks();
        for (DelegatedTaskDTO delegatedTaskValue : task)
        {
            sharedDelegationsModel.assignToMe(delegatedTaskValue.task().get().identity());
        }
    }

    @Action
    public void rejectUserDelegations() throws ResourceException
    {
        Iterable<DelegatedTaskDTO> task = sharedDelegationsView.getSelectedTasks();
        for (DelegatedTaskDTO delegatedTaskValue : task)
        {
            sharedDelegationsModel.reject(delegatedTaskValue.task().get().identity());
        }
    }

    // Shared user waiting for actions ------------------------------
    @Action
    public void delegateWaitingForTask()
    {

    }

    // Group administration actions ---------------------------------
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
        uowf.nestedUnitOfWork();
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
        uowf.nestedUnitOfWork();
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