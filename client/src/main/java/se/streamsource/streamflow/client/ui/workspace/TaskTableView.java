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
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.FontHighlighter;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Base class for all views of task lists.
 */
public abstract class TaskTableView
        extends JPanel
        implements KeyEventDispatcher
{
    @Service
    DialogService dialogs;

    @Uses
    protected ObjectBuilder<UserOrProjectSelectionDialog> userOrProjectSelectionDialog;

    protected
    @Service
    StreamFlowApplication application;

    protected JXTable taskTable;
    protected TaskTableModel model;
    private TaskDetailView detailsView;
    private JPanel noTaskSelected;

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

        noTaskSelected = new JPanel();
        noTaskSelected.setMaximumSize(new Dimension(0,0));
        noTaskSelected.setOpaque(true);

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

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        add(toolbar, BorderLayout.NORTH);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(150);
        taskTable.getColumn(2).setMaxWidth(150);
        taskTable.setAutoCreateColumnsFromModel(false);

        splitPane.setTopComponent(taskScrollPane);
        splitPane.setBottomComponent(noTaskSelected);
        splitPane.setResizeWeight(0.3D);

        JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
        taskTable.setDefaultEditor(Boolean.class, completableEditor);
        taskTable.setDefaultRenderer(Date.class, new DefaultTableRenderer(new StringValue()
        {
            private SimpleDateFormat format = new SimpleDateFormat();

            public String getString(Object value)
            {
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

        taskTable.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(taskTable.getSearchable()).newInstance());

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
                getSelectedTask().description().set(newValue);
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
                            splitPane.setBottomComponent(noTaskSelected);
//                            splitPane.setDividerLocation(1D);
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
                            if (splitPane.getBottomComponent() != detailsView)
                            {
                                splitPane.setBottomComponent(detailsView);
                            }
                            TaskDetailModel taskModel = model.taskDetailModel(dto.task().get().identity());
                            taskModel.general().addObserver(descriptionUpdater);
                            taskModel.refresh();

                            detailsView.setTaskModel(taskModel);

                            if (detailsView.getSelectedIndex() != -1)
                                model.markAsRead(taskTable.getSelectedRow());
                        }
                    } catch (Exception e1)
                    {
                        throw new OperationException(WorkspaceResources.could_not_view_details, e1);
                    }
                }
            }
        });
        splitPane.setDividerLocation(1D);
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

    public TaskDTO getSelectedTask()
    {
        int selectedRow = getTaskTable().getSelectedRow();
        if (selectedRow == -1)
            return null;
        else
            return model.getTask(selectedRow);
    }

    public Iterable<TaskDTO> getSelectedTasks()
    {
        int[] rows = getTaskTable().getSelectedRows();
        List<TaskDTO> tasks = new ArrayList<TaskDTO>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = rows[i];
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
        int index = model.getRowCount()-1;
        table.getSelectionModel().setSelectionInterval(index, index);
        table.scrollRowToVisible(index);

        detailsView.setSelectedIndex(0);
        detailsView.getComponentAt(0).requestFocus();
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

    @org.jdesktop.application.Action()
    public void markTasksAsUnread() throws ResourceException
    {
        for (int row : getReverseSelectedTasks())
        {
            model.markAsUnread(row);
        }
    }

    @org.jdesktop.application.Action()
    public void markTasksAsRead() throws ResourceException
    {
        for (int row : getReverseSelectedTasks())
        {
            model.markAsRead(row);
        }
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
            list.add(0, row);
        }
        return list;
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }


    @Override
    public void removeNotify()
    {
        super.removeNotify();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
    }

    public boolean dispatchKeyEvent(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_C && e.isAltDown() && e.isControlDown())
        {
            if (!taskTable.getSelectionModel().isSelectionEmpty())
            {
                model.completeTask(taskTable.getSelectedRow());
                return true;
            }
        }
        return false;
    }
}