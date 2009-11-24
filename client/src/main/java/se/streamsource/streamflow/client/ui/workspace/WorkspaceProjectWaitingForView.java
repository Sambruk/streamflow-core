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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.task.TasksDetailView;

import javax.swing.*;

/**
 * JAVADOC
 */
public class WorkspaceProjectWaitingForView
        extends TaskTableView
{
    @Uses
    protected ObjectBuilder<SelectUserOrProjectDialog> userOrProjectSelectionDialog;

    public void init(@Service ApplicationContext context,
            @Uses final TaskTableModel model,
            final @Uses TasksDetailView detailsView,
            @Structure final ObjectBuilderFactory obf,
            @Structure ValueBuilderFactory vbf)
    {
    	super.init(context, model, detailsView, obf, vbf);
        taskTable.getColumn(3).setPreferredWidth(150);
        taskTable.getColumn(3).setMaxWidth(150);
    }
    
    protected void buildPopupMenu(JPopupMenu popup)
    {
        ActionMap am = getActionMap();
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
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action delegateTasksFromWaitingFor = addToolbarButton(toolbar, "delegateTasks");
        Action acceptAction = addToolbarButton(toolbar, "completeTasks");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(assignAction, delegateTasksFromWaitingFor, acceptAction));
    }

    @Override
    @org.jdesktop.application.Action
    public void delegateTasks() throws ResourceException
    {
        SelectUserOrProjectDialog dialog = userOrProjectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        dialogSelection = dialog.getSelected();
        super.delegateTasks();
    }

    @org.jdesktop.application.Action
    public void completeTasks()
    {
        for (int row : getReverseSelectedTasks())
        {
            ((WorkspaceProjectWaitingForModel)model).complete(row);
        }
        model.refresh();
    }
}
