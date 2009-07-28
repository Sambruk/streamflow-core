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
import org.jdesktop.swingx.JXTreeTable;
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
import se.streamsource.streamflow.resource.inbox.NewTaskCommand;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.FontHighlighter;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.resource.label.LabelDTO;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JAVADOC
 */
public class UserInboxView
        extends JTabbedPane
{
    @Service
    DialogService dialogs;

    @Uses
    private ObjectBuilder<AddTaskDialog> addTaskDialogs;
    @Uses
    private ObjectBuilder<ForwardTasksDialog> forwardTasksDialog;
    @Uses
    private ObjectBuilder<DelegateTasksDialog> delegateTasksDialog;

    private
    @Service
    StreamFlowApplication application;

    private JXTreeTable taskTable;
    private UserInboxModel model;
    private UserInboxTaskDetailModel detailModel;


    private LabelsModel labelsModel;
    private JComboBox labelsList;

    public UserInboxView(@Uses final LabelsModel labelsModel,
                         @Service ApplicationContext context,
                         @Uses final UserInboxModel model,
                         @Service final UserInboxTaskDetailView detailView,
                         @Service final UserInboxTaskDetailModel detailModel,
                         @Structure ObjectBuilderFactory obf,
                         @Structure ValueBuilderFactory vbf)
    {
        super();
        this.labelsModel = labelsModel;
        this.model = model;
        this.detailModel = detailModel;

        labelsList = new JComboBox(labelsModel);
        labelsList.setRenderer(new DefaultListRenderer(new StringValue()
        {
            public String getString(Object o)
            {
                return o == null ? "" : ((LabelDTO) o).description().get();
            }
        }));

        ActionMap am = context.getActionMap(this);

        TasksQuery query = vbf.newValue(TasksQuery.class);
        try
        {
            model.setQuery(query);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }

        // Toolbar
        JPanel top = new JPanel(new GridLayout(2,1));
        top.setBorder(BorderFactory.createEtchedBorder());
        JPanel toolbar = new JPanel();

        javax.swing.Action addAction = am.get("newTask");
        toolbar.add(new JButton(addAction));
        Action assignAction = am.get("assignTasksToMe");
        toolbar.add(new JButton(assignAction));
        Action delegateTasksFromInbox = am.get("delegateTasks");
        toolbar.add(new JButton(delegateTasksFromInbox));
        javax.swing.Action refreshAction = am.get("refresh");
        toolbar.add(new JButton(refreshAction));

        top.add(toolbar);
        top.add(labelsList);

        // Table
        JPanel panel = new JPanel(new BorderLayout());
        taskTable = new JXTreeTable(model);
        taskTable.setRootVisible(false);
        taskTable.setSortable(true);

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        panel.add(top, BorderLayout.NORTH);
        panel.add(taskScrollPane, BorderLayout.CENTER);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(150);
        taskTable.getColumn(2).setMaxWidth(150);
        taskTable.setAutoCreateColumnsFromModel(false);

        JPanel det = new JPanel();
        det.add(detailView);
        addTab(text(inbox_tab), panel);
        addTab(text(detail_tab), detailView);
        setEnabledAt(1, false);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "details");
        getActionMap().put("details", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setSelectedIndex(1);
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "inbox");
        getActionMap().put("inbox", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setSelectedIndex(0);
            }
        });

        taskTable.setLeafIcon(null);
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
                return componentAdapter != null && !(Boolean) componentAdapter.getValue(3);
            }
        }, taskTable.getFont().deriveFont(Font.BOLD), taskTable.getFont()));

        taskTable.addHighlighter(new ColorHighlighter(new HighlightPredicate()
        {
            public boolean isHighlighted(Component component, ComponentAdapter componentAdapter)
            {
                return componentAdapter != null && (Boolean) componentAdapter.getValue(4);
            }
        }, Color.black, Color.lightGray));
        taskTable.setEditable(true);
        taskTable.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath selectionPath = taskTable.getTreeSelectionModel().getSelectionPath();
                if (selectionPath != null)
                {
                    setEnabledAt(1, true);
                } else
                    setEnabledAt(1, false);
            }
        });

        taskTable.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                try
                {
                    Iterable<InboxTaskDTO> task = getSelectedTasks();
                    for (InboxTaskDTO taskValue : task)
                    {
                        if (!taskValue.isRead().get())
                        {
                            model.markAsRead(taskValue.task().get().identity());
                            taskValue.isRead().set(true);
                        }
                    }
                } catch (ResourceException e1)
                {
                    e1.printStackTrace();
                }
            }
        });

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
        final JMenu labelMenu = new JMenu(i18n.text(WorkspaceResources.labels_label));
        labelMenu.addMenuListener(new MenuListener()
        {
            public void menuSelected(MenuEvent e)
            {
                labelMenu.removeAll();

                List<LabelDTO> labels = getSelectedTask().labels().get().labels().get();
                int size = labelsModel.getSize();
                for (int i = 0; i < size; i++)
                {
                    final LabelDTO label = labelsModel.getElementAt(i);
                    JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(new AbstractAction(label.description().get())
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e.getSource();
                            Iterable<InboxTaskDTO> taskDTOs = getSelectedTasks();
                            for (InboxTaskDTO taskDTO : taskDTOs)
                            {
                                if (checkBoxMenuItem.getState())
                                {
                                    String taskId = taskDTO.task().get().identity();
                                    try
                                    {
                                        model.addLabel(taskId, label.label().get().identity());
                                        taskDTO.labels().get().labels().get().add(label);
                                    } catch (ResourceException e1)
                                    {
                                        e1.printStackTrace();
                                    }
                                } else
                                {
                                    String taskId = taskDTO.task().get().identity();
                                    try
                                    {
                                        model.removeLabel(taskId, label.label().get().identity());
                                        taskDTO.labels().get().labels().get().remove(label);
                                    } catch (ResourceException e1)
                                    {
                                        e1.printStackTrace();
                                    }
                                }

                            }
                        }
                    });
                    boolean state = false;
                    for (LabelDTO labelDTO : labels)
                    {
                        if (labelDTO.label().get().equals(label.label().get()))
                            state = true;
                    }
                    checkBoxMenuItem.setState(state);
                    labelMenu.add(checkBoxMenuItem);
                }
            }

            public void menuDeselected(MenuEvent e)
            {
            }

            public void menuCanceled(MenuEvent e)
            {
            }
        });
        popup.add(labelMenu);
        popup.add(am.get("markTasksAsUnread"));
        Action dropAction = am.get("dropTasks");
        popup.add(dropAction);
        Action removeTaskAction = am.get("removeTasks");
        popup.add(removeTaskAction);
        popup.add(am.get("forwardTasksTo"));
        taskTable.addMouseListener(new PopupMenuTrigger(popup));
        taskTable.addTreeSelectionListener(new SelectionActionEnabler(dropAction, removeTaskAction, assignAction, delegateTasksFromInbox));
    }

    @Override
    public void setSelectedIndex(int index)
    {
        try
        {
            if (index == 1)
            {
                InboxTaskDTO dto = getSelectedTask();
                detailModel.setResource(model.getRoot().task(dto.task().get().identity()));
            }
            super.setSelectedIndex(index);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    public JXTreeTable getTaskTable()
    {
        return taskTable;
    }

    public InboxTaskDTO getSelectedTask()
    {
        int selectedRow = getTaskTable().getSelectedRow();
        if (selectedRow == -1)
            return null;
        else
            return (InboxTaskDTO) getTaskTable().getPathForRow(selectedRow).getLastPathComponent();
    }

    public Iterable<InboxTaskDTO> getSelectedTasks()
    {
        int[] rows = getTaskTable().getSelectedRows();
        List<InboxTaskDTO> tasks = new ArrayList<InboxTaskDTO>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = rows[i];
            InboxTaskDTO task = (InboxTaskDTO) getTaskTable().getPathForRow(row).getLastPathComponent();
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        setSelectedIndex(0);
    }

    @org.jdesktop.application.Action()
    public void newTask() throws ResourceException
    {
        // Show dialog
        AddTaskDialog dialog = addTaskDialogs.newInstance();
        dialogs.showOkCancelHelpDialog(application.getMainFrame(), dialog);

        NewTaskCommand command = dialog.getCommandBuilder().newInstance();

        model.newTask(command);

        JXTreeTable table = getTaskTable();
        int index = model.getChildCount(model.getRoot());
        Object child = model.getChild(model, index - 1);
        TreePath path = new TreePath(child);
        table.getSelectionModel().clearSelection();
        table.getSelectionModel().addSelectionInterval(index - 1, index - 1);
        table.scrollPathToVisible(path);
    }

    @org.jdesktop.application.Action()
    public void dropTasks() throws ResourceException
    {
        Iterable<InboxTaskDTO> selectedTasks = getSelectedTasks();
        for (InboxTaskDTO selectedTask : selectedTasks)
        {
            model.dropTask(selectedTask.task().get().identity());
        }
    }

    @org.jdesktop.application.Action()
    public void markTasksAsUnread() throws ResourceException
    {
        int[] rows = taskTable.getSelectedRows();
        for (int row : rows)
        {
            model.markAsUnread(row);
        }
    }


    @org.jdesktop.application.Action()
    public void addSubTask()
    {
        // Show dialog
        AddTaskDialog dialog = addTaskDialogs.newInstance();
        InboxTaskDTO selected = getSelectedTask();
        dialog.getCommandBuilder().prototype().parentTask().set(selected.task().get());
        dialogs.showOkCancelHelpDialog(application.getMainFrame(), dialog);
    }

    @org.jdesktop.application.Action
    public void assignTasksToMe() throws ResourceException
    {
        int selection = getTaskTable().getSelectedRow();
        Iterable<InboxTaskDTO> selectedTasks = getSelectedTasks();
        for (InboxTaskDTO selectedTask : selectedTasks)
        {
            model.assignToMe(selectedTask.task().get().identity());
        }
        getTaskTable().getSelectionModel().setSelectionInterval(selection, selection);
        repaint();
    }

    @org.jdesktop.application.Action
    public void delegateTasks() throws ResourceException
    {
        dialogs.showOkCancelHelpDialog(application.getMainFrame(), delegateTasksDialog.newInstance());
    }

    @org.jdesktop.application.Action
    public void removeTasks() throws ResourceException
    {
        Iterable<InboxTaskDTO> selected = getSelectedTasks();
        for (InboxTaskDTO taskValue : selected)
        {
            model.removeTask(taskValue.task().get().identity());
        }
    }

    @org.jdesktop.application.Action
    public void forwardTasksTo()
    {
        dialogs.showOkCancelHelpDialog(application.getMainFrame(), forwardTasksDialog.newInstance());
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
}
