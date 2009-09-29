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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.FontHighlighter;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
import se.streamsource.streamflow.client.ui.workspace.UserOrProjectSelectionDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Base class for all views of task lists.
 */
public abstract class TaskTableView
        extends JPanel
{
    @Service
    protected DialogService dialogs;

    @Uses
    protected ObjectBuilder<UserOrProjectSelectionDialog> userOrProjectSelectionDialog;
    
    @Service
    protected StreamFlowApplication application;

    protected JXTable taskTable;
    protected TaskTableModel model;
    private TaskDetailView detailsView;

    public void init(@Service ApplicationContext context,
                     @Uses final TaskTableModel model,
                     final @Uses TaskDetailView detailsView,
                     @Structure final ObjectBuilderFactory obf,
                     @Structure ValueBuilderFactory vbf)
    {
        this.model = model;
        this.detailsView = detailsView;
        setLayout(new BorderLayout());
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);

        ActionMap am = context.getActionMap(TaskTableView.class, this);
        setActionMap(am);

        TasksQuery query = vbf.newValue(TasksQuery.class);
        try
        {
            model.setQuery(query);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }

        // Toolbar
        JPanel toolbar = new JPanel();

        // Table
        taskTable = new JXTable(model);
        taskTable.getActionMap().getParent().setParent(am);
        taskTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
          KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        taskTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
          KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        add(toolbar, BorderLayout.NORTH);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(150);
        taskTable.getColumn(2).setMaxWidth(150);
        taskTable.setAutoCreateColumnsFromModel(false);

        splitPane.setTopComponent(taskScrollPane);
        splitPane.setBottomComponent(detailsView);
        splitPane.setResizeWeight(0.3D);

        JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
        taskTable.setDefaultEditor(Boolean.class, completableEditor);
        taskTable.setDefaultRenderer(Date.class, new DefaultTableRenderer(new StringValue()
        {
            private SimpleDateFormat format = new SimpleDateFormat();

            public String getString(Object value)
            {
                if (value == null) return "";
                Date time = (Date) value;
                return format.format(time);
            }
        }));

        taskTable.addHighlighter(HighlighterFactory.createAlternateStriping());
        taskTable.addHighlighter(new FontHighlighter(new HighlightPredicate()
        {
            public boolean isHighlighted(Component component, ComponentAdapter componentAdapter)
            {
                return componentAdapter != null && !(Boolean) componentAdapter.getValue(TaskTableModel.IS_READ);
            }
        }, taskTable.getFont().deriveFont(Font.BOLD), taskTable.getFont()));

        taskTable.addHighlighter(new ColorHighlighter(new HighlightPredicate()
        {
            public boolean isHighlighted(Component component, ComponentAdapter componentAdapter)
            {
                return componentAdapter != null && (Boolean) componentAdapter.getValue(TaskTableModel.IS_DROPPED);
            }
        }, Color.black, Color.lightGray));
        taskTable.setEditable(true);

        // Popup
        JPopupMenu popup = new JPopupMenu();
        buildPopupMenu(popup);
        taskTable.addMouseListener(new PopupMenuTrigger(popup, taskTable.getSelectionModel()));
        buildToolbar(toolbar);

        // Update description of task when updated
        final Observer descriptionUpdater = new Observer()
        {
            public void update(Observable o, Object arg)
            {
                TaskGeneralModel generalModel = (TaskGeneralModel) arg;
                String newValue = generalModel.getGeneral().description().get();
                model.setValueAt(newValue, taskTable.convertRowIndexToModel(taskTable.getSelectedRow()), 1);
            }
        };

        taskTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    try
                    {
                        if (detailsView.getTaskModel() != null)
                            detailsView.getTaskModel().general().deleteObserver(descriptionUpdater);

                        if (taskTable.getSelectionModel().isSelectionEmpty())
                        {
                            detailsView.setTaskModel(null);
                        } else
                        {
                            TaskDTO dto = null;
                            try
                            {
                                dto = getSelectedTask();
                            } catch (Exception e1)
                            {
                                // Ignore
                                return;
                            }
                            TaskDetailModel taskModel = model.taskDetailModel(dto.task().get().identity());
                            taskModel.general().addObserver(descriptionUpdater);
//                            taskModel.refresh();

                            detailsView.setTaskModel(taskModel);

                            if (detailsView.getSelectedIndex() != -1)
                                model.markAsRead(taskTable.convertRowIndexToModel(taskTable.getSelectedRow()));
                        }
                    } catch (Exception e1)
                    {
                        throw new OperationException(WorkspaceResources.could_not_view_details, e1);
                    }
                }
            }
        });
        splitPane.setDividerLocation(1D);

        addFocusListener(new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                taskTable.requestFocusInWindow();
            }
        });
    }

    abstract protected void buildPopupMenu(JPopupMenu popup);

    abstract protected void buildToolbar(JPanel toolbar);

    protected Action addToolbarButton(JPanel toolbar, String name)
    {
        ActionMap am = getActionMap();
        Action action = am.get(name);
        action.putValue(Action.SMALL_ICON, i18n.icon((ImageIcon) action.getValue(Action.SMALL_ICON), 16));
        toolbar.add(new JButton(action));
        return action;
    }

    public JXTable getTaskTable()
    {
        return taskTable;
    }

    public TaskDetailView getTaskDetail()
    {
        return detailsView;
    }

    public TaskDTO getSelectedTask()
    {
        int selectedRow = getTaskTable().getSelectedRow();
        if (selectedRow == -1)
            return null;
        else
            return model.getTask(getTaskTable().convertRowIndexToModel(selectedRow));
    }

    public Iterable<TaskDTO> getSelectedTasks()
    {
        int[] rows = getTaskTable().getSelectedRows();
        List<TaskDTO> tasks = new ArrayList<TaskDTO>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = getTaskTable().convertRowIndexToModel(rows[i]);
            TaskDTO task = (TaskDTO) model.getTask(row);
            tasks.add(task);
        }
        return tasks;
    }

    @org.jdesktop.application.Action()
    public void createTask() throws ResourceException
    {
        model.createTask();
        model.refresh();

        JXTable table = getTaskTable();
        int index = model.getRowCount() - 1;
        table.getSelectionModel().setSelectionInterval(index, index);
        table.scrollRowToVisible(index);

        detailsView.setSelectedIndex(0);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Component component1 = detailsView.getSelectedComponent();
                if (component1 != null)
                    component1.requestFocusInWindow();
            }
        });
    }

    @org.jdesktop.application.Action()
    public void dropTasks() throws ResourceException
    {
        for (Integer row : getReverseSelectedTasks())
        {
            model.dropTask(row);
        }
        model.refresh();
    }

    @org.jdesktop.application.Action(block = Task.BlockingScope.ACTION)
    public Task markTasksAsUnread() throws ResourceException
    {
        return new Task(Application.getInstance())
        {
            protected Object doInBackground() throws Exception
            {
                for (int row : getReverseSelectedTasks())
                {
                    model.markAsUnread(row);
                }
                return null;
            }
        };
    }

    @org.jdesktop.application.Action(block = Task.BlockingScope.ACTION)
    public Task markTasksAsRead() throws ResourceException
    {
        return new Task(Application.getInstance())
        {
            protected Object doInBackground() throws Exception
            {
                for (int row : getReverseSelectedTasks())
                {
                    model.markAsRead(row);
                }
                return null;
            }
        };
    }

    @org.jdesktop.application.Action
    public void removeTasks() throws ResourceException
    {
        for (int row : getReverseSelectedTasks())
        {
            model.removeTask(row);
        }
        model.refresh();
    }

    @org.jdesktop.application.Action
    public void completeTasks()
    {
        for (int row : getReverseSelectedTasks())
        {
            model.completeTask(row);
        }
    }

    @org.jdesktop.application.Action
    public void refresh() throws ResourceException
    {
        model.refresh();
    }

    protected List<Integer> getReverseSelectedTasks()
    {
        int[] rows = taskTable.getSelectedRows();
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = rows[i];
            list.add(taskTable.convertRowIndexToModel(row));
        }
        sort(list, reverseOrder());
        return list;
    }
}