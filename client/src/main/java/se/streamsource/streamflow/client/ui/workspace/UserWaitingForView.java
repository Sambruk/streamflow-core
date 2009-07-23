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
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JAVADOC
 */
public class UserWaitingForView
        extends JTabbedPane
{
    private JXTreeTable taskTable;
    private UserWaitingForModel model;
    private UserWaitingForTaskDetailModel detailModel;

    public UserWaitingForView(@Service ApplicationContext context,
                              @Service final UserWaitingForModel model,
                           @Service final UserWaitingForTaskDetailView detailView,
                           @Service final UserWaitingForTaskDetailModel detailModel,
                           @Structure ObjectBuilderFactory obf,
                           @Structure ValueBuilderFactory vbf)
    {
        super();
        this.model = model;
        this.detailModel = detailModel;

        ActionMap am = context.getActionMap(this);

        // Popup
        JPopupMenu popup = new JPopupMenu();
        Action removeAction = am.get("removeTasks");
        popup.add(removeAction);

        // Table
        JPanel panel = new JPanel(new BorderLayout());
        taskTable = new JXTreeTable(model);
        taskTable.setRootVisible(false);
        taskTable.setSortable(true);

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        panel.add(taskScrollPane, BorderLayout.CENTER);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(4).setPreferredWidth(120);
        taskTable.getColumn(4).setMaxWidth(120);
        taskTable.setAutoCreateColumnsFromModel(false);

        addTab(text(waitingfor_tab), panel);
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
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "waitingfor");
        getActionMap().put("waitingfor", new AbstractAction()
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

        taskTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    setSelectedComponent(detailView);
                }
            }
        });

        taskTable.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(taskTable.getSearchable()).newInstance());

        taskTable.addHighlighter(HighlighterFactory.createAlternateStriping());
        taskTable.addHighlighter(new FontHighlighter(new HighlightPredicate()
        {
            public boolean isHighlighted(Component component, ComponentAdapter componentAdapter)
            {
                return !(Boolean) componentAdapter.getValue(5);
            }
        }, taskTable.getFont().deriveFont(Font.BOLD), taskTable.getFont()));

        taskTable.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                try
                {
                    Iterable<WaitingForTaskDTO> task = getSelectedTasks();
                    for (WaitingForTaskDTO taskValue : task)
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


        // Toolbar
        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEtchedBorder());
        Action delegateAction = am.get("delegateTasks");
        toolbar.add(new JButton(delegateAction));
        Action refreshAction = am.get("refresh");
        toolbar.add(new JButton(refreshAction));
        panel.add(toolbar, BorderLayout.NORTH);

        taskTable.addTreeSelectionListener(new SelectionActionEnabler(delegateAction, removeAction));
    }

    public JXTreeTable getTaskTable()
    {
        return taskTable;
    }

    public WaitingForTaskDTO getSelectedTask()
    {
        int selectedRow = getTaskTable().getSelectedRow();
        if (selectedRow == -1)
            return null;
        else
            return (WaitingForTaskDTO) getTaskTable().getPathForRow(selectedRow).getLastPathComponent();
    }

    public Iterable<WaitingForTaskDTO> getSelectedTasks()
    {
        int[] rows = getTaskTable().getSelectedRows();
        List<WaitingForTaskDTO> tasks = new ArrayList<WaitingForTaskDTO>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = rows[i];
            WaitingForTaskDTO task = (WaitingForTaskDTO) getTaskTable().getPathForRow(row).getLastPathComponent();
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public void setSelectedIndex(int index)
    {
        try
        {
            if (index == 1)
            {
                WaitingForTaskDTO dto = getSelectedTask();
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

    @Override
    public void addNotify()
    {
        super.addNotify();

        setSelectedIndex(0);
    }

    @org.jdesktop.application.Action
    public void removeTasks()
    {
        // TODO
    }

    @org.jdesktop.application.Action
    public void delegateTasks()
    {
        // TODO
    }

    @org.jdesktop.application.Action
    public void refresh() throws ResourceException
    {
        model.refresh();
    }

}