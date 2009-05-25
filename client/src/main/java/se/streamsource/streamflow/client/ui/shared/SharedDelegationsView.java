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

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.search.Searchable;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.StreamFlowApplication;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.shared.SharedDelegationsResources.*;
import se.streamsource.streamflow.client.ui.status.StatusBarView;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * JAVADOC
 */
public class SharedDelegationsView
        extends JTabbedPane
{
    private JXTreeTable taskTable;

    @Service
    StreamFlowApplication application;
    private Searchable searchTable;

    public SharedDelegationsView(@Service SharedInboxToolbarView toolbarView,
                                 @Service final SharedDelegationsModel model,
                                 @Service final SharedTaskView detailView,
                                 @Service final SharedTaskModel detailModel)
    {
        super();

        model.setColumnIdentifiers(Arrays.asList(text(description_column_header),
                text(created_column_header),
                text(delegated_from_header),
                text(delegated_by_header)));

        JPanel panel = new JPanel(new BorderLayout());
        taskTable = new JXTreeTable(model)
        {
            @Override
            public String getStringAt(int row, int column)
            {
                if (column == 0)
                {
                    Object value = getValueAt(row, column);
                    return value.toString();
                }

                return super.getStringAt(row, column);
            }
        };

        JScrollPane taskScrollPane = new JScrollPane(taskTable);

        panel.add(taskScrollPane, BorderLayout.CENTER);
        panel.add(toolbarView, BorderLayout.SOUTH);


        taskTable.getColumnModel().getColumn(1).setMaxWidth(150);
        taskTable.getColumnModel().getColumn(2).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(2).setResizable(false);
        taskTable.setAutoCreateColumnsFromModel(false);

        searchTable = taskTable.getSearchable();

        addTab(text(delegations_tab), panel);
        addTab(text(detail_tab), detailView);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "details");
        getActionMap().put("details", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setSelectedIndex(1);
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "delegations");
        getActionMap().put("delegations", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                setSelectedIndex(0);
            }
        });

        taskTable.getColumn(1).setCellRenderer(new DefaultTableRenderer(new StringValue()
        {
            private SimpleDateFormat format = new SimpleDateFormat();

            public String getString(Object value)
            {
                long time = (Long) value;
                return format.format(new Date(time));
            }
        }));
        taskTable.getColumn(2).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        JXTable.BooleanEditor completableEditor = new JXTable.BooleanEditor();
        taskTable.setDefaultEditor(Boolean.class, completableEditor);
        taskTable.setEditable(true);
        taskTable.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath selectionPath = taskTable.getTreeSelectionModel().getSelectionPath();
                if (selectionPath != null)
                {
                    setEnabledAt(1, true);
                    InboxTaskNode node = (InboxTaskNode) selectionPath.getLastPathComponent();
//                    SharedTask task = (SharedTask) node.getUserObject();
//                    detailModel.setSharedTask(task, model.callAuthentication());
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

        taskTable.addTreeSelectionListener(toolbarView);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        StatusBarView statusBarView = (StatusBarView) ((JXFrame) application.getMainFrame()).getStatusBar();
        statusBarView.getSearchField().setSearchable(searchTable);
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
}