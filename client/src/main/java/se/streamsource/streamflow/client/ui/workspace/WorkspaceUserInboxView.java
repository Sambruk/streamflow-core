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
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemTableCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.task.TasksDetailView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * JAVADOC
 */
public class WorkspaceUserInboxView
        extends TaskTableView implements ItemListener
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
        taskTable.putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);

        JComboBox projectsCombo = new JComboBox(((WorkspaceUserInboxModel)model).getProjectsModel());
        projectsCombo.addItemListener(this);
        projectsCombo.setRenderer(new ListItemListCellRenderer());
        TableColumn column = taskTable.getColumnModel().getColumn(1);
        column.setCellEditor(new ComboBoxCellEditor(projectsCombo));
        column.setCellRenderer(new ListItemTableCellRenderer());
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
    }

    @Override
    protected void buildToolbar(JPanel toolbar)
    {
        addToolbarButton(toolbar, "createTask");
        Action acceptAction = addToolbarButton(toolbar, "completeTasks");
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action forwardTasksFromInbox = addToolbarButton(toolbar, "forwardTasks");
        Action delegateTasksFromInbox = addToolbarButton(toolbar, "delegateTasks");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(assignAction, forwardTasksFromInbox, delegateTasksFromInbox, acceptAction));
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

    @Override
    @org.jdesktop.application.Action
    public void forwardTasks() throws ResourceException
    {
        SelectUserOrProjectDialog dialog = userOrProjectSelectionDialog.newInstance();
        dialogs.showOkCancelHelpDialog(this, dialog);

        dialogSelection = dialog.getSelected();
        super.forwardTasks();
    }

    public void forwardTask(String identity) throws ResourceException
    {
    	((WorkspaceUserInboxModel)model).forward(getTaskTable().getSelectedRow(), identity);
    }
    

	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.DESELECTED) 
		{
			// Event type not of interest.
			return;
		}
		if (e.getSource() instanceof JComboBox)
		{
			JComboBox combo = (JComboBox)e.getSource();
			if(combo.getModel() instanceof ProjectSelectorModel)
			{
				ProjectSelectorModel model = (ProjectSelectorModel)combo.getModel();
				ListItemValue project = (ListItemValue)model.getSelectedItem();
				String identity = project.entity().get().identity();
				try
				{
					forwardTask(identity);
					if(taskTable.getCellEditor() != null)
					{
						taskTable.getCellEditor().stopCellEditing();
					}
				} catch (ResourceException e1)
				{
					throw new OperationException(WorkspaceResources.could_not_forward_task_to_project, e1);
				}
			}
		}
	}
}
