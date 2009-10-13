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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.task.TaskTableView;

import javax.swing.*;

/**
 * JAVADOC
 */
public class WorkspaceProjectInboxView
        extends TaskTableView
{
    @Uses
    protected ObjectBuilder<ProjectSelectionDialog> projectSelectionDialog;

    @Uses
    LabelMenu labelMenu;

    protected void buildPopupMenu(JPopupMenu popup)
    {
        taskTable.getSelectionModel().addListSelectionListener(labelMenu);

        ActionMap am = getActionMap();
        popup.add(labelMenu);
        popup.add(am.get("markTasksAsUnread"));
        popup.add(am.get("markTasksAsRead"));
        Action dropAction = am.get("dropTasks");
        popup.add(dropAction);
        Action removeTaskAction = am.get("removeTasks");
        popup.add(removeTaskAction);
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(dropAction, removeTaskAction));
    }

    @Override
    protected void buildToolbar(JPanel toolbar)
    {
        addToolbarButton(toolbar, "createTask");
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action forwardTasksFromInbox = addToolbarButton(toolbar, "forwardTasks");
        Action delegateTasksFromInbox = addToolbarButton(toolbar, "delegateTasks");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(assignAction, forwardTasksFromInbox, delegateTasksFromInbox));
    }

    @Override
    @org.jdesktop.application.Action
    public void delegateTasks() throws ResourceException
    {
        ProjectSelectionDialog dialog = projectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        dialogSelection = dialog.getSelected();
        super.delegateTasks();
    }

    @Override
    @org.jdesktop.application.Action
    public void forwardTasks() throws ResourceException
    {
        ProjectSelectionDialog dialog = projectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        dialogSelection = dialog.getSelected();
        super.forwardTasks();
    }
}