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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.FontHighlighter;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
import static se.streamsource.streamflow.client.ui.shared.SharedInboxResources.*;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;

/**
 * JAVADOC
 */
public class SharedInboxView
        extends JTabbedPane
{
    private JXTreeTable taskTable;

    public SharedInboxView(@Service final ActionMap am,
                           @Service final SharedInboxModel model,
                           @Service final SharedInboxTaskDetailView detailView,
                           @Service final SharedInboxTaskDetailModel detailModel,
                           @Structure ObjectBuilderFactory obf,
                           @Structure ValueBuilderFactory vbf)
    {
        super();

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
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        javax.swing.Action addAction = am.get("newSharedTask");
        toolbar.add(new JButton(addAction));
        Action assignAction = am.get("assignTasksToMe");
        toolbar.add(new JButton(assignAction));
        Action delegateTasksFromInbox = am.get("delegateTasksFromInbox");
        toolbar.add(new JButton(delegateTasksFromInbox));
        javax.swing.Action refreshAction = am.get("refreshSharedInbox");
        toolbar.add(new JButton(refreshAction));

        // Table
        JPanel panel = new JPanel(new BorderLayout());
        taskTable = new JXTreeTable(model);
        taskTable.setRootVisible(false);
        taskTable.setSortable(true);

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        panel.add(taskScrollPane, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(120);
        taskTable.getColumn(2).setMaxWidth(120);
        taskTable.setAutoCreateColumnsFromModel(false);

        JPanel det = new JPanel();
        det.add(detailView);
        addTab(text(inbox_tab), panel);
        addTab(text(detail_tab), detailView);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "details");
        getActionMap().put("details", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    detailModel.setResource(model.getRoot().task(getSelectedTask().task().get().identity()));
                    setSelectedIndex(1);
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                } catch (ResourceException e1)
                {
                    e1.printStackTrace();
                }
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
                return !(Boolean) componentAdapter.getValue(3);
            }
        }, taskTable.getFont().deriveFont(Font.BOLD), taskTable.getFont()));
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
                if (e.getClickCount() == 2)
                {
                    getActionMap().get("details").actionPerformed(null);
                }
            }
        });

        taskTable.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(taskTable.getSearchable()).newInstance());

        // Popup
        JPopupMenu popup = new JPopupMenu();
        Action removeTaskAction = am.get("removeSharedTasks");
        popup.add(removeTaskAction);
        popup.add(assignAction);
        popup.add(am.get("forwardSharedTasksTo"));
        taskTable.addMouseListener(new PopupMenuTrigger(popup));
        taskTable.addTreeSelectionListener(new SelectionActionEnabler(removeTaskAction, assignAction, delegateTasksFromInbox));
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
}
