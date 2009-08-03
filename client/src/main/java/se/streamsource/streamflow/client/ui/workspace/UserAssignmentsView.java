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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * JAVADOC
 */
public class UserAssignmentsView
        extends TaskTableView
{
    @Uses LabelMenu labelMenu;

    protected String tabName()
    {
        return text(assignments_tab);
    }

    protected void buildPopupMenu(JPopupMenu popup, ActionMap am)
    {
        taskTable.getSelectionModel().addListSelectionListener(labelMenu);

        popup.add(labelMenu);
        popup.add(am.get("markTasksAsUnread"));
        Action dropAction = am.get("dropTasks");
        popup.add(dropAction);
        Action removeTaskAction = am.get("removeTasks");
        popup.add(removeTaskAction);
        popup.add(am.get("forwardTasks"));
        taskTable.addMouseListener(new PopupMenuTrigger(popup));
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(dropAction, removeTaskAction));
    }

    @Override
    protected void buildToolbar(JPanel toolbar, ActionMap am)
    {
        javax.swing.Action addAction = am.get("newTask");
        toolbar.add(new JButton(addAction));
        Action delegateTasks = am.get("delegateTasks");
        toolbar.add(new JButton(delegateTasks));
        javax.swing.Action refreshAction = am.get("refresh");
        toolbar.add(new JButton(refreshAction));
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(delegateTasks));
    }

    @org.jdesktop.application.Action
    public void delegateTasks() throws ResourceException
    {
        UserOrProjectSelectionDialog dialog = userOrProjectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        EntityReference selected = dialog.getSelected();
        if (selected != null)
        {
            int[] rows = taskTable.getSelectedRows();
            for (int row : rows)
            {
                model.delegate(row, selected.identity());
            }
        }
    }

    @org.jdesktop.application.Action
    public void forwardTasks() throws ResourceException
    {
        UserOrProjectSelectionDialog dialog = userOrProjectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        EntityReference selected = dialog.getSelected();
        if (selected != null)
        {
            int[] rows = taskTable.getSelectedRows();
            for (int row : rows)
            {
                model.forward(row, selected.identity());
            }
        }
    }
}