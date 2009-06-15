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
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.shared.SharedResources.assignments_tab;
import static se.streamsource.streamflow.client.ui.shared.SharedResources.detail_tab;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
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
public class SharedAssignmentsView
        extends JTabbedPane
{
    private JXTreeTable taskTable;
    private SharedAssignmentsTaskDetailModel detailModel;
    private SharedAssignmentsModel model;


    public SharedAssignmentsView(@Service final ActionMap am,
                           @Service final SharedAssignmentsModel model,
                           @Service final SharedAssignmentsTaskDetailView detailView,
                           @Service SharedAssignmentsTaskDetailModel detailModel,
                           @Structure ObjectBuilderFactory obf,
                           @Structure ValueBuilderFactory vbf)
    {
        super();

        this.detailModel = detailModel;
        this.model = model;
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
        javax.swing.Action refreshAction = am.get("refreshSharedAssignments");
        toolbar.add(new JButton(refreshAction));

        // Table
        JPanel panel = new JPanel(new BorderLayout());
        taskTable = new JXTreeTable(model);
        taskTable.setRootVisible(false);
        taskTable.setSortable(true);

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(taskScrollPane, BorderLayout.CENTER);


        taskTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        taskTable.getColumn(0).setMaxWidth(30);
        taskTable.getColumn(0).setResizable(false);
        taskTable.getColumn(2).setPreferredWidth(120);
        taskTable.getColumn(2).setMaxWidth(120);
        taskTable.setAutoCreateColumnsFromModel(false);

        addTab(text(assignments_tab), panel);
        addTab(text(detail_tab), detailView);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "details");
        getActionMap().put("details", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setSelectedIndex(1);
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "assignments");
        getActionMap().put("assignments", new AbstractAction()
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

        // Popup
        JPopupMenu popup = new JPopupMenu();
        Action removeAction = am.get("removeSharedTasks");
        popup.add(removeAction);

        taskTable.addTreeSelectionListener(new SelectionActionEnabler(removeAction));
    }


    @Override
    public void setSelectedIndex(int index)
    {
        try
        {
            if (index == 1)
            {
                AssignedTaskDTO dto = getSelectedTask();
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

    public AssignedTaskDTO getSelectedTask()
    {
        int row = getTaskTable().getSelectedRow();
        if (row == -1)
        {
            return null;
        }
        return (AssignedTaskDTO) getTaskTable().getPathForRow(row).getLastPathComponent();
    }

    public Iterable<AssignedTaskDTO> getSelectedTasks()
    {
        int[] rows = getTaskTable().getSelectedRows();
        List<AssignedTaskDTO> tasks = new ArrayList<AssignedTaskDTO>();
        for (int i = 0; i < rows.length; i++)
        {
            int row = rows[i];
            AssignedTaskDTO task = (AssignedTaskDTO) getTaskTable().getPathForRow(row).getLastPathComponent();
            tasks.add(task);
        }
        return tasks;
    }

    class CompletedCellRenderer
            extends JCheckBox
            implements TableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            return this;
        }
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        setSelectedIndex(0);
    }
}