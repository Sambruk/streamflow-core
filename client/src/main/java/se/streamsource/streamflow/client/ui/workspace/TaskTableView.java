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
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
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
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    DialogService dialogs;

    @Uses
    protected ObjectBuilder<AddTaskDialog> addTaskDialogs;

    @Uses
    protected ObjectBuilder<UserOrProjectSelectionDialog> userOrProjectSelectionDialog;

    protected
    @Service
    StreamFlowApplication application;

    protected JXTable taskTable;
    protected TaskTableModel model;


    protected LabelsModel labelsModel;
    protected JComboBox labelsList;

    public void init(@Service ApplicationContext context,
                         @Uses LabelsModel labelsModel,
                         @Uses final TaskTableModel model,
                         final @Uses TaskDetailView detailsView,
                         @Structure final ObjectBuilderFactory obf,
                         @Structure ValueBuilderFactory vbf)
    {
        this.model = model;
        setLayout(new BorderLayout());
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);

        labelsList = new JComboBox(labelsModel);
        labelsList.setRenderer(new DefaultListRenderer(new StringValue()
        {
            public String getString(Object o)
            {
                return o == null ? "" : ((ListItemValue) o).description().get();
            }
        }));

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
        taskScrollPane.setMinimumSize(new Dimension(400, 100));
        taskScrollPane.setPreferredSize(new Dimension(400, 400));

        add(toolbar, BorderLayout.NORTH);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(150);
        taskTable.getColumn(2).setMaxWidth(150);
        taskTable.setAutoCreateColumnsFromModel(false);

        splitPane.setTopComponent(taskScrollPane);
        splitPane.setBottomComponent(detailsView);
        splitPane.setResizeWeight(0.3);

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

        taskTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
                {
                    getActionMap().get("details").actionPerformed(null);
                }
            }
        });

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
                getSelectedTask().description().set(generalModel.getGeneral().description().get());
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
                        if (taskTable.getSelectionModel().isSelectionEmpty())
                        {
                            if (detailsView.getTaskModel() != null)
                                detailsView.getTaskModel().general().deleteObserver(descriptionUpdater);
                            detailsView.setTaskModel(null);
                        } else
                        {
                            TaskDTO dto = getSelectedTask();
                            TaskDetailModel taskModel = model.taskDetailModel(dto.task().get().identity());
                            taskModel.general().addObserver(descriptionUpdater);
                            taskModel.refresh();

                            detailsView.setTaskModel(taskModel);
                        }
                    } catch (Exception e1)
                    {
                        throw new OperationException(WorkspaceResources.could_not_view_details, e1);
                    }
                }
            }
        });
    }
    
    abstract protected void buildPopupMenu(JPopupMenu popup);

    abstract protected void buildToolbar(JPanel toolbar);

    protected Action addToolbarButton(JPanel toolbar, String name)
    {
        ActionMap am = getActionMap();
        Action action = am.get(name);
        action.putValue(Action.LARGE_ICON_KEY, i18n.icon((ImageIcon) action.getValue(Action.LARGE_ICON_KEY), 16));
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
    public Task refresh() throws ResourceException
    {
        return new Task(application)
        {
            protected Object doInBackground() throws Exception
            {
                model.refresh();
                return null;
            }
        };
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
}