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

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemTableCellRenderer;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.task.TasksDetailView;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

/**
 * JAVADOC
 */
public class WorkspaceUserInboxView
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
        taskTable.putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);
        taskTable.setEditingColumn( 1 );

        TableColumn column = taskTable.getColumnModel().getColumn(1);
                
        final AutoCompleteSupport.AutoCompleteCellEditor editor = AutoCompleteSupport.createTableCellEditor(new TableFormat<ListItemValue>()
        {
            public int getColumnCount()
            {
                return 0;
            }

            public String getColumnName( int i )
            {
                return "";
            }

            public Object getColumnValue( ListItemValue listItemValue, int i )
            {
                return listItemValue.description().get();
            }
        }, ((WorkspaceUserInboxModel)model).getProjectsModel().getList(), 1);
        editor.getAutoCompleteSupport().setStrict( true );
        editor.getAutoCompleteSupport().setSelectsTextOnFocusGain( true );
        editor.setClickCountToStart( 1 );
        final JComboBox combo = (JComboBox) editor.getComponent();

        column.setCellEditor( editor );
        column.setCellRenderer(new ListItemTableCellRenderer());
    }
    
    protected void buildPopupMenu(JPopupMenu popup)
    {
        ActionMap am = getActionMap();
        Action markTasksAsUnread = am.get( "markTasksAsUnread" );
        popup.add( markTasksAsUnread );
        Action markTasksAsRead = am.get( "markTasksAsRead" );
        popup.add( markTasksAsRead );
        Action dropAction = am.get("dropTasks");
        popup.add(dropAction);
        Action removeTaskAction = am.get("removeTasks");
        popup.add(removeTaskAction);
        taskTable.getSelectionModel().addListSelectionListener(new TaskSelectionActionEnabler(3, taskTable, markTasksAsRead, markTasksAsUnread, dropAction, removeTaskAction));
    }

    @Override
    protected void buildToolbar(JPanel toolbar)
    {
        addToolbarButton(toolbar, "createTask");
        Action completeAction = addToolbarButton(toolbar, "completeTasks");
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action forwardTasksFromInbox = addToolbarButton(toolbar, "forwardTasks");
        Action delegateTasksFromInbox = addToolbarButton(toolbar, "delegateTasks");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new TaskSelectionActionEnabler(3, taskTable, assignAction, forwardTasksFromInbox, delegateTasksFromInbox, completeAction));
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
}
