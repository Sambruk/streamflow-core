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

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * JAVADOC
 */
public class UserWaitingForView
        extends TaskTableView
{
    @Uses LabelMenu labelMenu;

    protected void buildPopupMenu(JPopupMenu popup)
    {
        taskTable.getSelectionModel().addListSelectionListener(labelMenu);

        ActionMap am = getActionMap();
        popup.add(labelMenu);
        popup.add(am.get("markTasksAsUnread"));
        Action dropAction = am.get("dropTasks");
        popup.add(dropAction);
        Action removeTaskAction = am.get("removeTasks");
        popup.add(removeTaskAction);
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(dropAction, removeTaskAction));
    }

    @Override
    protected void buildToolbar(JPanel toolbar)
    {
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action delegateTasksFromInbox = addToolbarButton(toolbar, "delegateTasks");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(assignAction, delegateTasksFromInbox));
    }

    @org.jdesktop.application.Action
    public void assignTasksToMe() throws ResourceException
    {
        int selection = getTaskTable().getSelectedRow();
        int[] rows = taskTable.getSelectedRows();
        for (int row : rows)
        {
            model.assignToMe(row);
        }
        getTaskTable().getSelectionModel().setSelectionInterval(selection, selection);
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
}
